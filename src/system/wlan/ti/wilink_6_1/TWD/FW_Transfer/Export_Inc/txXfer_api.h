/*
 * txXfer_api.h
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
 *   MODULE:  txXfer_api.h
 *
 *   PURPOSE: Tx Xfer module API.
 * 
 ****************************************************************************/

#ifndef _TX_XFER_API_H
#define _TX_XFER_API_H


#include "TWDriver.h"


/* Public Function Definitions */

TI_HANDLE       txXfer_Create (TI_HANDLE hOs);
TI_STATUS       txXfer_Destroy (TI_HANDLE hTxXfer);
TI_STATUS       txXfer_Init (TI_HANDLE hTxXfer, TI_HANDLE hReport, TI_HANDLE hTwIf);
TI_STATUS       txXfer_Restart (TI_HANDLE hTxXfer, TI_BOOL bChipIs1273Pg10);
ETxnStatus      txXfer_SendPacket (TI_HANDLE hTxXfer, TTxCtrlBlk *pPktCtrlBlk);
void            txXfer_RegisterCb (TI_HANDLE hTxResult, TI_UINT32 CallBackID, void *CBFunc, TI_HANDLE CBObj);

#endif /* _TX_XFER_API_H */




