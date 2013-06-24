/*
 * FwEvent.c
 *
 * Copyright(c) 1998 - 2009 Texas Instruments. All rights reserved.      
 * All rights reserved.                                                  
 *                                                                       
 * Redistribution and use in source and binary forms, with or without    
 * modification, are permitted provided that the following conditions    
 * are met:                                                              
 *                                                                       
 *  * Redistributions of source code must retain the above copyright     
 *    notice, this list of conditions and the following disclaimer.      
 *  * Redistributions in binary form must reproduce the above copyright  
 *    notice, this list of conditions and the following disclaimer in    
 *    the documentation and/or other materials provided with the         
 *    distribution.                                                      
 *  * Neither the name Texas Instruments nor the names of its            
 *    contributors may be used to endorse or promote products derived    
 *    from this software without specific prior written permission.      
 *                                                                       
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT     
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT      
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT   
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


/** \file  FwEvent.c
 *  \brief Handle firmware events
 * 
 *   
 * \par Description
 *      Call the appropriate event handler.
 *
 *  \see FwEvent.h
 */

#define __FILE_ID__  FILE_ID_104
#include "tidef.h"
#include "report.h"
#include "context.h"
#include "osApi.h"
#include "TWDriver.h"
#include "TWDriverInternal.h"
#include "txResult_api.h"
#include "CmdMBox_api.h"
#include "rxXfer_api.h" 
#include "txXfer_api.h" 
#include "txHwQueue_api.h"
#include "eventMbox_api.h"
#include "TwIf.h"
#include "public_host_int.h"
#include "FwEvent_api.h"
#ifdef TI_DBG
    #include "tracebuf_api.h"
#endif
#include "bmtrace_api.h"


#ifdef _VLCT_
extern int trigger_another_read;
#endif


#define FW_STATUS_ADDR (0x14FC0 + 0xA000)

#define ALL_EVENTS_VECTOR        ACX_INTR_WATCHDOG | ACX_INTR_INIT_COMPLETE | ACX_INTR_EVENT_A |\
                                 ACX_INTR_EVENT_B | ACX_INTR_CMD_COMPLETE |ACX_INTR_HW_AVAILABLE |\
                                 ACX_INTR_DATA

#define ALL_EVENTS_VECTOR_NEGATE 0xFFFFFFC0   


#define TXN_FW_EVENT_SET_MASK_ADDR(pFwEvent)      pFwEvent->tMaskTxn.tTxnStruct.uHwAddr = HINT_MASK;
#define TXN_FW_EVENT_SET_UNMASK_ADDR(pFwEvent)    pFwEvent->tUnMaskTxn.tTxnStruct.uHwAddr = HINT_MASK;
#define TXN_FW_EVENT_SET_FW_STAT_ADDR(pFwEvent)   pFwEvent->tFwStatusTxn.tTxnStruct.uHwAddr = FW_STATUS_ADDR;


typedef enum
{
    FW_EVENT_STATE_IDLE,    
    FW_EVENT_STATE_READING

} EFwEventState;

typedef struct 
{
    TTxnStruct              tTxnStruct;
    TI_UINT32               uData; 

} TRegisterTxn;

typedef struct 
{
    TTxnStruct     tTxnStruct;
    FwStatus_t     tFwStatus;

} TFwStatusTxn;

/* The FwEvent module's main structure */
typedef struct 
{
    EFwEventState       eFwEventState;           	/* State machine state */
    TI_UINT32           uEventMask;              	/* Static interrupt event mask */
    TI_UINT32           uEventVector;               /* Saves the current active FW interrupts */
    TI_BOOL             bIsActualFwInterrupt;       /* Indicates that we are working on a real interrupt from the FW */
    TRegisterTxn        tMaskTxn; 
    TRegisterTxn        tUnMaskTxn;
    TFwStatusTxn        tFwStatusTxn;               /* The FW status structure transaction (read from FW memory) */
        
    TI_UINT32           uFwTimeOffset;              /* Offset in microseconds between driver and FW clocks */
    TI_BOOL             bEmulateRxIntr;             /* Indicate to call Rx interrupt handler even if not issued */

    /* Other modules handles */
    TI_HANDLE           hOs;                    	
    TI_HANDLE           hTWD;
    TI_HANDLE           hReport;                	
    TI_HANDLE           hContext;               	
    TI_UINT32           uContextId;             	
    TI_HANDLE           hTwIf;                      
    TI_HANDLE           hHealthMonitor;             
    TI_HANDLE           hEventMbox;
    TI_HANDLE           hCmdMbox;
    TI_HANDLE           hRxXfer;
    TI_HANDLE           hTxXfer;
    TI_HANDLE           hTxHwQueue;
    TI_HANDLE           hTxResult;

} TfwEvent; 


static void fwEvent_CallHandler     (TI_HANDLE hFwEvent);
static void fwEvent_Handle          (TI_HANDLE hFwEvent);
static void fwEvent_ReadCompleteCb  (TI_HANDLE hFwEvent);




/*
 * \brief	Create the FwEvent module object
 * 
 * \param  hOs  - OS module object handle
 * \return Handle to the created object
 * 
 * \par Description
 * Calling this function creates a FwEvent object
 * 
 * \sa fwEvent_Destroy
 */
TI_HANDLE fwEvent_Create (TI_HANDLE hOs)
{
    TfwEvent *pFwEvent;

    pFwEvent = os_memoryAlloc (hOs, sizeof(TfwEvent));
    if (pFwEvent == NULL)
    {
        return NULL;
    }

    os_memoryZero (hOs, pFwEvent, sizeof(TfwEvent));

    pFwEvent->hOs = hOs;

    return (TI_HANDLE)pFwEvent;
}


/*
 * \brief	Destroys the FwEvent object
 * 
 * \param  hFwEvent  - The object to free
 * \return TI_OK
 * 
 * \par Description
 * Calling this function destroys a FwEvent object
 * 
 * \sa fwEvent_Create
 */
TI_STATUS fwEvent_Destroy (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;

    if (pFwEvent)
    {
        os_memoryFree (pFwEvent->hOs, pFwEvent, sizeof(TfwEvent));
    }

    return TI_OK;
}


/*
 * \brief	Config the FwEvent module object
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \param  hTWD  - Handle to TWD module
 * \return TI_OK
 * 
 * \par Description
 * From hTWD we extract : hOs, hReport, hTwIf, hContext,
 *      hHealthMonitor, hEventMbox, hCmdMbox, hRxXfer, 
 *      hTxHwQueue, hTxResult
 * In this function we also register the FwEvent to the context engine
 * 
 * \sa
 */
TI_STATUS fwEvent_Init (TI_HANDLE hFwEvent, TI_HANDLE hTWD)
{
    TfwEvent  *pFwEvent = (TfwEvent *)hFwEvent;
    TTwd      *pTWD = (TTwd *)hTWD;
    TTxnStruct* pTxn;
    
    pFwEvent->hTWD              = hTWD;
    pFwEvent->hOs               = pTWD->hOs;
    pFwEvent->hReport           = pTWD->hReport;
    pFwEvent->hContext          = pTWD->hContext;
    pFwEvent->hTwIf             = pTWD->hTwIf;
    pFwEvent->hHealthMonitor    = pTWD->hHealthMonitor;
    pFwEvent->hEventMbox        = pTWD->hEventMbox;
    pFwEvent->hCmdMbox          = pTWD->hCmdMbox;
    pFwEvent->hRxXfer           = pTWD->hRxXfer;
    pFwEvent->hTxHwQueue        = pTWD->hTxHwQueue;
    pFwEvent->hTxXfer           = pTWD->hTxXfer;
    pFwEvent->hTxResult         = pTWD->hTxResult;

    pFwEvent->eFwEventState       = FW_EVENT_STATE_IDLE;
    pFwEvent->uEventMask          = 0;
    pFwEvent->uEventVector        = 0;
    pFwEvent->bIsActualFwInterrupt = TI_FALSE;
    pFwEvent->bEmulateRxIntr      = TI_FALSE;

    pTxn = (TTxnStruct*)&pFwEvent->tMaskTxn.tTxnStruct;
    TXN_PARAM_SET(pTxn, TXN_HIGH_PRIORITY, TXN_FUNC_ID_WLAN, TXN_DIRECTION_WRITE, TXN_INC_ADDR)
    BUILD_TTxnStruct(pTxn, HINT_MASK, &pFwEvent->tMaskTxn.uData, REGISTER_SIZE, NULL, NULL)

    pTxn = (TTxnStruct*)&pFwEvent->tUnMaskTxn.tTxnStruct;
    TXN_PARAM_SET(pTxn, TXN_HIGH_PRIORITY, TXN_FUNC_ID_WLAN, TXN_DIRECTION_WRITE, TXN_INC_ADDR)
    BUILD_TTxnStruct(pTxn, HINT_MASK, &pFwEvent->tUnMaskTxn.uData, REGISTER_SIZE, NULL, NULL)


    pTxn = (TTxnStruct*)&pFwEvent->tFwStatusTxn.tTxnStruct;
    TXN_PARAM_SET(pTxn, TXN_HIGH_PRIORITY, TXN_FUNC_ID_WLAN, TXN_DIRECTION_READ, TXN_INC_ADDR)
    BUILD_TTxnStruct(pTxn, FW_STATUS_ADDR, &pFwEvent->tFwStatusTxn.tFwStatus, sizeof(FwStatus_t), (TTxnDoneCb)fwEvent_ReadCompleteCb, hFwEvent)

    /* 
     *  Register the FwEvent to the context engine and get the client ID.
     *  The FwEvent() will be called from the context_DriverTask() after scheduled
     *    by a FW-Interrupt (see fwEvent_InterruptRequest()).
     */
    pFwEvent->uContextId = context_RegisterClient (pFwEvent->hContext,
                                                   fwEvent_Handle,
                                                   hFwEvent,
                                                   TI_FALSE,
                                                   "FW_EVENT",
                                                   sizeof("FW_EVENT"));
	
    return TI_OK;
}


/*
 * \brief	Call FwEvent client's event handler
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 * 
 * \sa fwEvent_ReadCompleteCb
 */
static void fwEvent_CallHandler (TI_HANDLE hFwEvent)
{
    TfwEvent   *pFwEvent = (TfwEvent *)hFwEvent;

    if (pFwEvent->uEventVector & ACX_INTR_WATCHDOG)
    {
        /* Fw watchdog timeout has occured */
        TWD_WdExpireEvent (pFwEvent->hTWD);
    }

    if (pFwEvent->uEventVector & ACX_INTR_INIT_COMPLETE)
    {
        TRACE0(pFwEvent->hReport, REPORT_SEVERITY_INFORMATION, "fwEvent_CallHandler: INIT_COMPLETE\n");
    }
	/* Change to handle the command MBOX before the event MBOX to maintain order for WHA command response
	 * and follow command complete
	 */
    if (pFwEvent->uEventVector & ACX_INTR_CMD_COMPLETE)
    {
        /* Command Mbox completed */
        cmdMbox_CommandComplete(pFwEvent->hCmdMbox);
    }
    if (pFwEvent->uEventVector & ACX_INTR_EVENT_A)
    {
        eventMbox_Handle(pFwEvent->hEventMbox,&pFwEvent->tFwStatusTxn.tFwStatus);
    }
    if (pFwEvent->uEventVector & ACX_INTR_EVENT_B)
    {
        eventMbox_Handle(pFwEvent->hEventMbox,&pFwEvent->tFwStatusTxn.tFwStatus);
    }
    

    /* The DATA interrupt is shared by all data path events, so call all Tx and Rx clients */
    if (pFwEvent->uEventVector & ACX_INTR_DATA)
    {
        rxXfer_RxEvent (pFwEvent->hRxXfer, &pFwEvent->tFwStatusTxn.tFwStatus);

        txHwQueue_UpdateFreeResources (pFwEvent->hTxHwQueue, &pFwEvent->tFwStatusTxn.tFwStatus);

        txResult_TxCmpltIntrCb (pFwEvent->hTxResult, &pFwEvent->tFwStatusTxn.tFwStatus);
    } 
    else if (pFwEvent->bEmulateRxIntr) 
    {
        pFwEvent->bEmulateRxIntr = TI_FALSE;
        rxXfer_RxEvent (pFwEvent->hRxXfer, &pFwEvent->tFwStatusTxn.tFwStatus);
    }

    /* After handling all raised bits, we can negate them */
    pFwEvent->tFwStatusTxn.tFwStatus.intrStatus &= pFwEvent->uEventMask;
}


/*
 * \brief	Requests the context engine to schedule the driver task
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 * Called by the FW-Interrupt ISR.
 * Requests the context engine to schedule the driver task 
 * for handling the FW-Events (FwEvent callback).
 * 
 * \sa
 */
void fwEvent_InterruptRequest (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;
    CL_TRACE_START_L1();

    /* Indicate that we are handling an actual FW interrupt (for FW time setting) */
    pFwEvent->bIsActualFwInterrupt = TI_TRUE;

    /* Request switch to driver context for handling the FW-Interrupt event */
    context_RequestSchedule (pFwEvent->hContext, pFwEvent->uContextId);

    CL_TRACE_END_L1("tiwlan_drv.ko", "FwEvent", "IRQ", "");
}


/*
 * \brief	Handle the FW interrupts
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 * Called from context module upon receiving FW interrupt
 * The function mask the interrupts and reads the FW status
 * 
 * \sa
 */

static void fwEvent_Handle (TI_HANDLE hFwEvent)
{
    TfwEvent   *pFwEvent = (TfwEvent *)hFwEvent;
    ETxnStatus rc;
    CL_TRACE_START_L2();

    if (pFwEvent->eFwEventState != FW_EVENT_STATE_IDLE)
    {
        if (pFwEvent->bIsActualFwInterrupt)
        {
            os_InterruptServiced (pFwEvent->hOs);
            twIf_HwAvailable(pFwEvent->hTwIf);
        }
        CL_TRACE_END_L2("tiwlan_drv.ko", "FwEvent", "Handle", "");
        return;
    }

    pFwEvent->eFwEventState = FW_EVENT_STATE_READING;

    twIf_Awake(pFwEvent->hTwIf);
	if (pFwEvent->bIsActualFwInterrupt)
    {
        twIf_HwAvailable(pFwEvent->hTwIf);
    }

    /* Write HINT mask */
    pFwEvent->tMaskTxn.uData = ACX_INTR_ALL;
    TXN_FW_EVENT_SET_MASK_ADDR(pFwEvent)
    twIf_Transact(pFwEvent->hTwIf, &(pFwEvent->tMaskTxn.tTxnStruct));


    /*
     * Read the Fw status
     */
    TXN_FW_EVENT_SET_FW_STAT_ADDR(pFwEvent)
    rc = twIf_TransactReadFWStatus(pFwEvent->hTwIf, &(pFwEvent->tFwStatusTxn.tTxnStruct));

    if (rc == TXN_STATUS_COMPLETE)
    {
        fwEvent_ReadCompleteCb(hFwEvent);
    }

    CL_TRACE_END_L2("tiwlan_drv.ko", "FwEvent", "Handle", "");
}


/*
 * \brief	Handle the Fw Status information 
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 * This function is called from fwEvent_Handle on a sync read, or from TwIf as a CB on an async read.
 * It calls fwEvent_CallHandler to handle the triggered interrupts.
 * 
 * \sa fwEvent_Handle
 */
static void fwEvent_ReadCompleteCb (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;
    
    os_InterruptServiced (pFwEvent->hOs);
	  
    /* If we were called because of an interrupt */
	if (pFwEvent->bIsActualFwInterrupt)
    {
        /* In case of level interrupt we need to clear the line */
        /*os_InterruptServiced(pFwEvent->hOs);*/

        /*
         * Sync to fw time so we can update the tx packets
         * on the delta time that they spent in the driver 
         */
        pFwEvent->uFwTimeOffset = (os_timeStampMs (pFwEvent->hOs) * 1000) - 
                                  ENDIAN_HANDLE_LONG (pFwEvent->tFwStatusTxn.tFwStatus.fwLocalTime);

        pFwEvent->bIsActualFwInterrupt = TI_FALSE;
    }

    /* Save the interrupts status retreived from the FW */
    pFwEvent->uEventVector = pFwEvent->tFwStatusTxn.tFwStatus.intrStatus;

    /* Mask unwanted interrupts */
    pFwEvent->uEventVector &= pFwEvent->uEventMask;

    /* Call the interrupts handlers */
    fwEvent_CallHandler(hFwEvent);

    /* Check if the state is changed in the context of the event callbacks */
    if (pFwEvent->eFwEventState == FW_EVENT_STATE_IDLE)
    {
        /*
         * When fwEvent_stop is called state is changed to IDLE
         * This is done in the context of the above events callbacks
         * Don't send the UNMASK transaction because the driver stop process includes power off
         */ 
        TRACE0(pFwEvent->hReport, REPORT_SEVERITY_WARNING, "fwEvent_ReadCompleteCb : State is IDLE ! don't send the UNMASK");
        return;
    }

    /* Write HINT unmask */
    pFwEvent->tUnMaskTxn.uData = ~pFwEvent->uEventMask;
    TXN_FW_EVENT_SET_UNMASK_ADDR(pFwEvent)
    twIf_Transact(pFwEvent->hTwIf, &(pFwEvent->tUnMaskTxn.tTxnStruct));

    twIf_Sleep(pFwEvent->hTwIf);
    pFwEvent->eFwEventState = FW_EVENT_STATE_IDLE;
} 


/*
 * \brief	Translate host to FW time (Usec)
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \param  uHostTime - The host time in MS to translate
 *
 * \return FW Time in Usec
 * 
 * \par Description
 * 
 * \sa
 */
TI_UINT32 fwEvent_TranslateToFwTime (TI_HANDLE hFwEvent, TI_UINT32 uHostTime)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;

    return ((uHostTime * 1000) - pFwEvent->uFwTimeOffset);
}


/*
 * \brief	Unmask only cmd-cmplt and events interrupts (needed for init phase)
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return Event mask
 * 
 * \par Description
 * Unmask only cmd-cmplt and events interrupts (needed for init phase)
 *                  and return interrupt enabled bit mask.
 * 
 * \sa
 */
TI_UINT32 fwEvent_GetInitMask (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;

    /* Unmask only the interrupts needed for the FW configuration process. */
    pFwEvent->uEventMask = ACX_INTR_CMD_COMPLETE | ACX_INTR_EVENT_A | ACX_INTR_EVENT_B;

    return pFwEvent->uEventMask;
}


/*
 * \brief	Stop & reset FwEvent (called by the driver stop process)
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return TI_OK
 * 
 * \par Description
 *
 * \sa
 */
TI_STATUS fwEvent_Stop (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;

    pFwEvent->eFwEventState = FW_EVENT_STATE_IDLE;
    pFwEvent->uEventMask = 0;
    pFwEvent->bIsActualFwInterrupt = TI_FALSE;
    pFwEvent->uEventVector = 0;    
    pFwEvent->bEmulateRxIntr = TI_FALSE;
    
    return TI_OK;
}


/*
 * \brief	Unmask all interrupts, set Rx interrupt bit and call FwEvent_Handle
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 * Called when driver Start or recovery process is completed.
 *              Unmask all interrupts, set Rx interrupt bit and call FwEvent_Handle 
 *                  (in case we missed an Rx interrupt in a recovery process).
 * 
 * \sa
 */
void fwEvent_EnableExternalEvents (TI_HANDLE hFwEvent)
{
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;

    /* Unmask all interrupts */
    pFwEvent->uEventMask = ALL_EVENTS_VECTOR;

    /* Set flag to invoke Rx interrupt handler in case we missed it in a recovery/start process */
    pFwEvent->bEmulateRxIntr = TI_TRUE;

    /* Handle interrupts including the Rx we've just set manually */
    fwEvent_Handle (hFwEvent);
}


/*
 * \brief	Disable the FwEvent client of the context thread handler
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 *
 * \sa
 */
void fwEvent_DisableInterrupts(TI_HANDLE hFwEvent)
{
    TfwEvent  *pFwEvent = (TfwEvent *)hFwEvent;

    context_DisableClient (pFwEvent->hContext,pFwEvent->uContextId);
	
}


/*
 * \brief	Enable the FwEvent client of the context thread handler
 * 
 * \param  hFwEvent  - FwEvent Driver handle
 * \return void
 * 
 * \par Description
 *
 * \sa
 */
void fwEvent_EnableInterrupts(TI_HANDLE hFwEvent)
{
    TfwEvent  *pFwEvent = (TfwEvent *)hFwEvent;

    context_EnableClient (pFwEvent->hContext,pFwEvent->uContextId);
	
}



#ifdef TI_DBG

void fwEvent_PrintStat (TI_HANDLE hFwEvent)
{
#ifdef REPORT_LOG
    TfwEvent *pFwEvent = (TfwEvent *)hFwEvent;
    FwStatus_t *fwStat = &pFwEvent->tFwStatusTxn.tFwStatus; 
    int i;

    WLAN_OS_REPORT(("Print FW event module info\n"));
    WLAN_OS_REPORT(("==========================\n"));
    WLAN_OS_REPORT(("intrStatus = 0x%08x\n", pFwEvent->uEventVector));
    WLAN_OS_REPORT(("intrMask   = 0x%08x\n", pFwEvent->uEventMask));
    WLAN_OS_REPORT(("counters   = 0x%08x\n", fwStat->counters));
	for (i = 0; i < NUM_RX_PKT_DESC; i++)
    {
		WLAN_OS_REPORT(("rxPktsDesc[%1d] = 0x%08x\n", i, fwStat->rxPktsDesc[i]));
    }
	for (i = 0; i < NUM_TX_QUEUES; i++)
    {
		WLAN_OS_REPORT(("txReleasedBlks[%1d] = 0x%08x\n", i, fwStat->txReleasedBlks[i]));
    }
    WLAN_OS_REPORT(("fwLocalTime = 0x%08x\n", fwStat->fwLocalTime));
#endif
}

#endif  /* TI_DBG */



