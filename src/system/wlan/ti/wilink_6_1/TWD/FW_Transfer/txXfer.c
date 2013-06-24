/*
 * txXfer.c
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


/****************************************************************************
 *
 *   MODULE:  txXfer.c
 *   
 *   PURPOSE: Handle Tx frame transfer to the firmware. 
 * 
 *   DESCRIPTION:  
 *   ============
 *      This module gets the upper driver's Tx packets after FW resources were
 *        allocated for it, and handles its transfer to the FW via the 
 *        host slave (indirect) interface, using the TwIf Transaction API.
 *
 ****************************************************************************/

#define __FILE_ID__  FILE_ID_108
#include "tidef.h"
#include "osApi.h"
#include "report.h"
#include "TwIf.h"
#include "TWDriver.h"
#include "FwEvent_api.h"
#include "txXfer_api.h"


/* remove workaround when WL6-PG1.0 becomes obsolete */
typedef struct 
{
    TTxnStruct              tTxnStruct;
    TI_UINT32               uPktsCntr; 
} TPktsCntrTxn;


/* The TxXfer module object. */
typedef struct 
{
    TI_HANDLE               hOs;
    TI_HANDLE               hReport;
    TI_HANDLE               hTwIf;

    TSendPacketTranferCb    fSendPacketTransferCb;   /* Upper layer Xfer-Complete callback */
    TI_HANDLE               hSendPacketTransferHndl; /* Upper layer Xfer-Complete callback handle */

    /* remove workaround when WL6-PG1.0 becomes obsolete */
    TI_BOOL                 bChipIs1273Pg10;
    TI_UINT32               uPktsCntr; 
    TPktsCntrTxn            aPktsCntrTxn[CTRL_BLK_ENTRIES_NUM]; 

} TTxXferObj;

static void txXfer_TransferDoneCb (TI_HANDLE hTxXfer, TTxnStruct *pTxn);



/****************************************************************************
 *                      txXfer_Create()
 ****************************************************************************
 * DESCRIPTION: Create the Xfer module object 
 * 
 * INPUTS:  None
 * 
 * OUTPUT:  None
 * 
 * RETURNS: The Created object
 ****************************************************************************/
TI_HANDLE txXfer_Create(TI_HANDLE hOs)
{
    TTxXferObj *pTxXfer;

    pTxXfer = os_memoryAlloc (hOs, sizeof(TTxXferObj));
    if (pTxXfer == NULL)
    {
        return NULL;
    }

    os_memoryZero (hOs, pTxXfer, sizeof(TTxXferObj));

    pTxXfer->hOs = hOs;

    return (TI_HANDLE)pTxXfer;
}


/****************************************************************************
 *                      txXfer_Destroy()
 ****************************************************************************
 * DESCRIPTION: Destroy the Xfer module object 
 * 
 * INPUTS:  hTxXfer - The object to free
 * 
 * OUTPUT:  None
 * 
 * RETURNS: TI_OK or TI_NOK
 ****************************************************************************/
TI_STATUS txXfer_Destroy(TI_HANDLE hTxXfer)
{
    TTxXferObj *pTxXfer = (TTxXferObj *)hTxXfer;

    if (pTxXfer)
    {
        os_memoryFree (pTxXfer->hOs, pTxXfer, sizeof(TTxXferObj));
    }

    return TI_OK;
}


/****************************************************************************
 *               txXfer_init()
 ****************************************************************************
   DESCRIPTION:  
   ============
     Initialize the Xfer module.
 ****************************************************************************/
TI_STATUS txXfer_Init (TI_HANDLE hTxXfer, TI_HANDLE hReport, TI_HANDLE hTwIf)
{
    TTxXferObj *pTxXfer = (TTxXferObj *)hTxXfer;
    TTxnStruct *pTxn;
    TI_UINT8    i;

    pTxXfer->hReport = hReport;
    pTxXfer->hTwIf   = hTwIf;
    pTxXfer->fSendPacketTransferCb = NULL;

    /* remove workaround when WL6-PG1.0 becomes obsolete */
    pTxXfer->uPktsCntr = 0;
    for (i = 0; i < CTRL_BLK_ENTRIES_NUM; i++)
    {
        pTxn = &(pTxXfer->aPktsCntrTxn[i].tTxnStruct);
        TXN_PARAM_SET(pTxn, TXN_LOW_PRIORITY, TXN_FUNC_ID_WLAN, TXN_DIRECTION_WRITE, TXN_INC_ADDR)
        BUILD_TTxnStruct(pTxn, HOST_WR_ACCESS_REG, &pTxXfer->aPktsCntrTxn[i].uPktsCntr, REGISTER_SIZE, NULL, NULL)
    }

    return txXfer_Restart(hTxXfer, TI_TRUE);
}


/****************************************************************************
 *               txXfer_Restart()
 ****************************************************************************
   DESCRIPTION:  
   ============
     Restart the Xfer module.
 ****************************************************************************/
TI_STATUS txXfer_Restart (TI_HANDLE hTxXfer, TI_BOOL bChipIs1273Pg10)
{
    TTxXferObj *pTxXfer = (TTxXferObj *)hTxXfer;

    /* remove the counter transactions workaround when WL6-PG1.0 becomes obsolete */
    pTxXfer->bChipIs1273Pg10 = bChipIs1273Pg10;
    pTxXfer->uPktsCntr = 0;

    return TI_OK;
}


/****************************************************************************
 *                  txXfer_sendPacket()
 ****************************************************************************
 * DESCRIPTION: 
   ============
   Send packet to the transaction queue.
   Return the transfer status:
     TXN_STATUS_COMPLETE - if completed, i.e. Synchronous mode.
     TXN_STATUS_PENDING  - if pending, i.e. Asynchronous mode. 
   Note that in case of PENDING, a callback function will be called 
       only if registered (needed for WHA).
 ****************************************************************************/
ETxnStatus txXfer_SendPacket (TI_HANDLE hTxXfer, TTxCtrlBlk *pPktCtrlBlk)
{
    TTxXferObj   *pTxXfer = (TTxXferObj *)hTxXfer;
    TTxnStruct   *pTxn    = (TTxnStruct *)pPktCtrlBlk;
    ETxnStatus   eStatus; 
    TPktsCntrTxn *pPktsCntrTxn; 
    
    /* Prepare the Txn fields to the host-slave register (fixed address) */
    TXN_PARAM_SET(pTxn, TXN_LOW_PRIORITY, TXN_FUNC_ID_WLAN, TXN_DIRECTION_WRITE, TXN_FIXED_ADDR)
    pTxn->uHwAddr = SLV_MEM_DATA;

    /* Fill the TxnDone CB only if registered by the upper layers */
    if (pTxXfer->fSendPacketTransferCb == NULL)
    {
        pTxn->fTxnDoneCb = NULL;
    }
    else 
    {
        pTxn->fTxnDoneCb = (TTxnDoneCb)txXfer_TransferDoneCb;
        pTxn->hCbHandle  = hTxXfer;
    }

    /* Send the transaction */
    eStatus = twIf_Transact (pTxXfer->hTwIf, pTxn);

#ifdef TI_DBG

    TRACE11(pTxXfer->hReport, REPORT_SEVERITY_INFORMATION, ": Status=%d, PktType=%d, Len0=%d, Len1=%d, Length=%d, ExtraBlks=%d, TotalBlks=%d, TxAttr=0x%x, TID=%d, DescID=%d, StartTime=%d\n", eStatus, pPktCtrlBlk->tTxPktParams.uPktType, pPktCtrlBlk->tTxnStruct.aLen[0], pPktCtrlBlk->tTxnStruct.aLen[1], pPktCtrlBlk->tTxDescriptor.length, pPktCtrlBlk->tTxDescriptor.extraMemBlks, pPktCtrlBlk->tTxDescriptor.totalMemBlks, pPktCtrlBlk->tTxDescriptor.txAttr, pPktCtrlBlk->tTxDescriptor.tid, pPktCtrlBlk->tTxDescriptor.descID, pPktCtrlBlk->tTxDescriptor.startTime);

    if (eStatus == TXN_STATUS_ERROR)
    {
        TI_UINT32 i;
        for (i = 0; i < MAX_XFER_BUFS; i++)
        {
            if (pPktCtrlBlk->tTxnStruct.aLen[i] == 0)
            {
                break;
            }
            TRACE1(pTxXfer->hReport, REPORT_SEVERITY_CONSOLE, "txXfer_SendPacket():  Tx Buffer %d:\n", i);
            WLAN_OS_REPORT (("txXfer_SendPacket():  Tx Buffer %d:\n", i));
            report_PrintDump(pPktCtrlBlk->tTxnStruct.aBuf[i], pPktCtrlBlk->tTxnStruct.aLen[i]);
            return eStatus;
        }
    }

#endif  /* TI_DBG */

    /* remove workaround when WL6-PG1.0 becomes obsolete */
    if (1) /* restore ->  if (pTxXfer->bChipIs1273Pg10) */
    {
        pTxXfer->uPktsCntr++;
        pPktsCntrTxn = &(pTxXfer->aPktsCntrTxn[pTxXfer->uPktsCntr % CTRL_BLK_ENTRIES_NUM]);
        pPktsCntrTxn->uPktsCntr = ENDIAN_HANDLE_LONG(pTxXfer->uPktsCntr);
        pPktsCntrTxn->tTxnStruct.uHwAddr = HOST_WR_ACCESS_REG;
        twIf_Transact(pTxXfer->hTwIf, &pPktsCntrTxn->tTxnStruct);
    }

    /* Return the Txn result - COMPLETE or PENDING. */
    /* Note: For PENDING, a callback function will be called only if registered (needed for WHA) */
    return eStatus;
}


/****************************************************************************
 *                      txXfer_TransferDoneCb()
 ****************************************************************************
 * DESCRIPTION:  Call the upper layers TranferDone callback, providing the TxCtrlBlk
 ****************************************************************************/
static void txXfer_TransferDoneCb (TI_HANDLE hTxXfer, TTxnStruct *pTxn)
{
    TTxXferObj *pTxXfer = (TTxXferObj*)hTxXfer;

    TRACE1(pTxXfer->hReport, REPORT_SEVERITY_INFORMATION, ": pTxn=0x%x\n", pTxn);

    /* Call the upper layers TranferDone callback, providing the TxCtrlBlk. */
    /* Note: If this CB was called it means that the upper CB exists (see in txXfer_SendPacket) */
    pTxXfer->fSendPacketTransferCb (pTxXfer->hSendPacketTransferHndl, (TTxCtrlBlk *)pTxn);
}


/****************************************************************************
 *                      txXfer_RegisterCb()
 ****************************************************************************
 * DESCRIPTION:  Register the upper driver Xfer callback functions.
 ****************************************************************************/
void txXfer_RegisterCb (TI_HANDLE hTxXfer, TI_UINT32 CallBackID, void *CBFunc, TI_HANDLE CBObj)
{
    TTxXferObj* pTxXfer = (TTxXferObj*)hTxXfer;

    TRACE3(pTxXfer->hReport, REPORT_SEVERITY_INFORMATION, ": CallBackID=%d, CBFunc=0x%x, CBObj=0x%x\n", CallBackID, CBFunc, CBObj);

    switch(CallBackID)
    {
        /* Save upper layers Transfer-Done callback */
        case TWD_INT_SEND_PACKET_TRANSFER:
            pTxXfer->fSendPacketTransferCb   = (TSendPacketTranferCb)CBFunc;
            pTxXfer->hSendPacketTransferHndl = CBObj;
            break;

        default:
            TRACE0(pTxXfer->hReport, REPORT_SEVERITY_ERROR, " - Illegal value\n");
            break;
    }
}





