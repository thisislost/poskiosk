/**
 * Constants for JavaPOS Applications.
 * 
 * Copyright (c) 2013 POSkiosk Team
 * 
 * Changes:
 * 
 * 28.03.2013 Maxim Ryabochkin Project skeleton
 * 
 */

define([], function() {

    return  {
        //###################################################################
        //#### General JavaPOS Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // "State" Property Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_S_CLOSED: 1,
        JPOS_S_IDLE: 2,
        JPOS_S_BUSY: 3,
        JPOS_S_ERROR: 4,
        /////////////////////////////////////////////////////////////////////
        // "ErrorCode" Property Constants
        /////////////////////////////////////////////////////////////////////

        JPOSERR: 100,
        JPOSERREXT: 200,
        JPOS_SUCCESS: 100,
        JPOS_E_CLOSED: 101,
        JPOS_E_CLAIMED: 102,
        JPOS_E_NOTCLAIMED: 103,
        JPOS_E_NOSERVICE: 104,
        JPOS_E_DISABLED: 105,
        JPOS_E_ILLEGAL: 106,
        JPOS_E_NOHARDWARE: 107,
        JPOS_E_OFFLINE: 108,
        JPOS_E_NOEXIST: 109,
        JPOS_E_EXISTS: 110,
        JPOS_E_FAILURE: 111,
        JPOS_E_TIMEOUT: 112,
        JPOS_E_BUSY: 113,
        JPOS_E_EXTENDED: 114,
        JPOS_E_DEPRECATED: 115, // 1.11


        /////////////////////////////////////////////////////////////////////
        // "ErrorCodeExtended" Property Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_ESTATS_ERROR: 280,
        JPOS_EFIRMWARE_BAD_FILE: 281,
        JPOS_ESTATS_DEPENDENCY: 282,
        /////////////////////////////////////////////////////////////////////
        // OPOS "BinaryConversion" Property Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_BC_NONE: 0,
        JPOS_BC_NIBBLE: 1,
        JPOS_BC_DECIMAL: 2,
        /////////////////////////////////////////////////////////////////////
        // "CheckHealth" Method: "Level" Parameter Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_CH_INTERNAL: 1,
        JPOS_CH_EXTERNAL: 2,
        JPOS_CH_INTERACTIVE: 3,
        /////////////////////////////////////////////////////////////////////
        // "CapPowerReporting", "PowerState", "PowerNotify" Property
        //   Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_PR_NONE: 0,
        JPOS_PR_STANDARD: 1,
        JPOS_PR_ADVANCED: 2,
        JPOS_PN_DISABLED: 0,
        JPOS_PN_ENABLED: 1,
        JPOS_PS_UNKNOWN: 2000,
        JPOS_PS_ONLINE: 2001,
        JPOS_PS_OFF: 2002,
        JPOS_PS_OFFLINE: 2003,
        JPOS_PS_OFF_OFFLINE: 2004,
        /////////////////////////////////////////////////////////////////////
        // "compareFirmwareVersion" Method: "result" Parameter Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_CFV_FIRMWARE_OLDER: 1,
        JPOS_CFV_FIRMWARE_SAME: 2,
        JPOS_CFV_FIRMWARE_NEWER: 3,
        JPOS_CFV_FIRMWARE_DIFFERENT: 4,
        JPOS_CFV_FIRMWARE_UNKNOWN: 5,
        /////////////////////////////////////////////////////////////////////
        // "ErrorEvent" Event: "ErrorLocus" Parameter Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_EL_OUTPUT: 1,
        JPOS_EL_INPUT: 2,
        JPOS_EL_INPUT_DATA: 3,
        /////////////////////////////////////////////////////////////////////
        // "ErrorEvent" Event: "ErrorResponse" Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_ER_RETRY: 11,
        JPOS_ER_CLEAR: 12,
        JPOS_ER_CONTINUEINPUT: 13,
        /////////////////////////////////////////////////////////////////////
        // "StatusUpdateEvent" Event: Common "Status" Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_SUE_POWER_ONLINE: 2001,
        JPOS_SUE_POWER_OFF: 2002,
        JPOS_SUE_POWER_OFFLINE: 2003,
        JPOS_SUE_POWER_OFF_OFFLINE: 2004,
        JPOS_SUE_UF_PROGRESS: 2100,
        JPOS_SUE_UF_COMPLETE: 2200, // JPOS_SUE_UF_PROGRESS + 100
        JPOS_SUE_UF_FAILED_DEV_OK: 2201,
        JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE: 2202,
        JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE: 2203,
        JPOS_SUE_UF_FAILED_DEV_UNKNOWN: 2204,
        JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED: 2205,
        /////////////////////////////////////////////////////////////////////
        // General Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_FOREVER: -1,
        //###################################################################
        //#### BillAcceptor Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // "FullStatus" Property Constants
        // "StatusUpdateEvent" Event Constants
        /////////////////////////////////////////////////////////////////////

        BACC_STATUS_OK: 0, // FullStatus

        BACC_STATUS_FULL: 21, // FullStatus, StatusUpdateEvent
        BACC_STATUS_NEARFULL: 22, // FullStatus, StatusUpdateEvent
        BACC_STATUS_FULLOK: 23, // StatusUpdateEvent

        BACC_STATUS_JAM: 31, // StatusUpdateEvent
        BACC_STATUS_JAMOK: 32, // StatusUpdateEvent


        /////////////////////////////////////////////////////////////////////
        // "DepositStatus" Property Constants
        /////////////////////////////////////////////////////////////////////

        BACC_STATUS_DEPOSIT_START: 1,
        BACC_STATUS_DEPOSIT_END: 2,
        BACC_STATUS_DEPOSIT_COUNT: 4,
        BACC_STATUS_DEPOSIT_JAM: 5,
        /////////////////////////////////////////////////////////////////////
        // "EndDeposit" Method Constants
        /////////////////////////////////////////////////////////////////////

        BACC_DEPOSIT_COMPLETE: 11,
        /////////////////////////////////////////////////////////////////////
        // "PauseDeposit" Method Constants
        /////////////////////////////////////////////////////////////////////

        BACC_DEPOSIT_PAUSE: 11,
        BACC_DEPOSIT_RESTART: 12,
        //###################################################################
        //#### BillDispenser Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // "DeviceStatus" Property Constants
        // "StatusUpdateEvent" Event Constants
        /////////////////////////////////////////////////////////////////////

        BDSP_STATUS_OK: 0, // DeviceStatus

        BDSP_STATUS_EMPTY: 11, // DeviceStatus, StatusUpdateEvent
        BDSP_STATUS_NEAREMPTY: 12, // DeviceStatus, StatusUpdateEvent
        BDSP_STATUS_EMPTYOK: 13, // StatusUpdateEvent

        BDSP_STATUS_JAM: 31, // DeviceStatus, StatusUpdateEvent
        BDSP_STATUS_JAMOK: 32, // StatusUpdateEvent

        BDSP_STATUS_ASYNC: 91, // StatusUpdateEvent


        /////////////////////////////////////////////////////////////////////
        // 'ResultCodeExtended' Property Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_EBDSP_OVERDISPENSE: 201, // DispenseCash

        /////////////////////////////////////////////////////////////////////
        // 'GateStatus' Property Constants
        /////////////////////////////////////////////////////////////////////

        GATE_GS_CLOSED: 1,
        GATE_GS_OPEN: 2,
        GATE_GS_BLOCKED: 3,
        GATE_GS_MALFUNCTION: 4,
        /////////////////////////////////////////////////////////////////////
        // 'StatusUpdateEvent' Event: 'Data' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        GATE_SUE_CLOSED: 11,
        GATE_SUE_OPEN: 12,
        GATE_SUE_BLOCKED: 13,
        GATE_SUE_MALFUNCTION: 14,
        //###################################################################
        //#### MSR Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // 'TracksToRead' Property Constants
        /////////////////////////////////////////////////////////////////////

        MSR_TR_NONE: 0,
        MSR_TR_1: 1,
        MSR_TR_2: 2,
        MSR_TR_3: 4,
        MSR_TR_4: 8,
        MSR_TR_1_2: 3,
        MSR_TR_1_3: 5,
        MSR_TR_1_4: 9,
        MSR_TR_2_3: 6,
        MSR_TR_2_4: 10,
        MSR_TR_3_4: 12,
        MSR_TR_1_2_3: 7,
        MSR_TR_1_2_4: 11,
        MSR_TR_1_3_4: 13,
        MSR_TR_2_3_4: 14,
        MSR_TR_1_2_3_4: 15,
        /////////////////////////////////////////////////////////////////////
        // 'ErrorReportingType' Property Constants
        /////////////////////////////////////////////////////////////////////

        MSR_ERT_CARD: 0,
        MSR_ERT_TRACK: 1,
        /////////////////////////////////////////////////////////////////////
        // 'CapDataEncryption', 'DataEncryptionAlgorithm' Property Constants
        //   (added in 1.12)
        /////////////////////////////////////////////////////////////////////

        MSR_DE_NONE: 0x00000001,
        MSR_DE_3DEA_DUKPT: 0x00000002,
        // Note: Service-specific values begin at 0x01000000.


        /////////////////////////////////////////////////////////////////////
        // 'CapDeviceAuthentication' Property Constants (added in 1.12)
        /////////////////////////////////////////////////////////////////////

        MSR_DA_NOT_SUPPORTED: 0,
        MSR_DA_OPTIONAL: 1,
        MSR_DA_REQUIRED: 2,
        /////////////////////////////////////////////////////////////////////
        // 'DeviceAuthenticationProtocol' Property Constants (added in 1.12)
        /////////////////////////////////////////////////////////////////////

        MSR_AP_NONE: 0,
        MSR_AP_CHALLENGERESPONSE: 1,
        /////////////////////////////////////////////////////////////////////
        // 'CardType' Property Constants (added in 1.12)
        /////////////////////////////////////////////////////////////////////

        MSR_CT_AAMVA: 'AAMVA',
        MSR_CT_BANK: 'BANK',
        /////////////////////////////////////////////////////////////////////
        // 'retrieveCardProperty' Parameter Constants (added in 1.12)
        /////////////////////////////////////////////////////////////////////

        MSR_RCP_AccountNumber: 'AccountNumber',
        MSR_RCP_Address: 'Address',
        MSR_RCP_BirthDate: 'BirthDate',
        MSR_RCP_City: 'City',
        MSR_RCP_Class: 'Class',
        MSR_RCP_Endorsements: 'Endorsements',
        MSR_RCP_ExpirationDate: 'ExpirationDate',
        MSR_RCP_EyeColor: 'EyeColor',
        MSR_RCP_FirstName: 'FirstName',
        MSR_RCP_Gender: 'Gender',
        MSR_RCP_HairColor: 'HairColor',
        MSR_RCP_Height: 'Height',
        MSR_RCP_LicenseNumber: 'LicenseNumber',
        MSR_RCP_MiddleInitial: 'MiddleInitial',
        MSR_RCP_PostalCode: 'PostalCode',
        MSR_RCP_Restrictions: 'Restrictions',
        MSR_RCP_ServiceCode: 'ServiceCode',
        MSR_RCP_State: 'State',
        MSR_RCP_Suffix: 'Suffix',
        MSR_RCP_Surname: 'Surname',
        MSR_RCP_Title: 'Title',
        MSR_RCP_Weight: 'Weight',
        /////////////////////////////////////////////////////////////////////
        // 'StatusUpdateEvent' Event: 'Data' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        MSR_SUE_DEVICE_AUTHENTICATED: 11, // 1.12
        MSR_SUE_DEVICE_DEAUTHENTICATED: 12, // 1.12


        /////////////////////////////////////////////////////////////////////
        // 'ErrorEvent' Event: 'ResultCodeExtended' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        JPOS_EMSR_START: 201,
        JPOS_EMSR_END: 202,
        JPOS_EMSR_PARITY: 203,
        JPOS_EMSR_LRC: 204,
        JPOS_EMSR_DEVICE_AUTHENTICATION_FAILED: 205, // 1.12
        JPOS_EMSR_DEVICE_DEAUTHENTICATION_FAILED: 206, // 1.12

        /////////////////////////////////////////////////////////////////////
        // 'CapDisplay' Property Constants
        /////////////////////////////////////////////////////////////////////

        PPAD_DISP_UNRESTRICTED: 1,
        PPAD_DISP_PINRESTRICTED: 2,
        PPAD_DISP_RESTRICTED_LIST: 3,
        PPAD_DISP_RESTRICTED_ORDER: 4,
        PPAD_DISP_NONE: 5,
        /////////////////////////////////////////////////////////////////////
        // 'AvailablePromptsList' and 'Prompt' Property Constants
        /////////////////////////////////////////////////////////////////////

        PPAD_MSG_ENTERPIN: 1,
        PPAD_MSG_PLEASEWAIT: 2,
        PPAD_MSG_ENTERVALIDPIN: 3,
        PPAD_MSG_RETRIESEXCEEDED: 4,
        PPAD_MSG_APPROVED: 5,
        PPAD_MSG_DECLINED: 6,
        PPAD_MSG_CANCELED: 7,
        PPAD_MSG_AMOUNTOK: 8,
        PPAD_MSG_NOTREADY: 9,
        PPAD_MSG_IDLE: 10,
        PPAD_MSG_SLIDE_CARD: 11,
        PPAD_MSG_INSERTCARD: 12,
        PPAD_MSG_SELECTCARDTYPE: 13,
        /////////////////////////////////////////////////////////////////////
        // 'CapLanguage' Property Constants
        /////////////////////////////////////////////////////////////////////

        PPAD_LANG_NONE: 1,
        PPAD_LANG_ONE: 2,
        PPAD_LANG_PINRESTRICTED: 3,
        PPAD_LANG_UNRESTRICTED: 4,
        /////////////////////////////////////////////////////////////////////
        // 'TransactionType' Property Constants
        /////////////////////////////////////////////////////////////////////

        PPAD_TRANS_DEBIT: 1,
        PPAD_TRANS_CREDIT: 2,
        PPAD_TRANS_INQ: 3,
        PPAD_TRANS_RECONCILE: 4,
        PPAD_TRANS_ADMIN: 5,
        /////////////////////////////////////////////////////////////////////
        // 'EndEFTTransaction' Method Completion Code Constants
        /////////////////////////////////////////////////////////////////////

        PPAD_EFT_NORMAL: 1,
        PPAD_EFT_ABNORMAL: 2,
        /////////////////////////////////////////////////////////////////////
        // 'DataEvent' Event Status Constants
        /////////////////////////////////////////////////////////////////////
        PPAD_SUCCESS: 1,
        PPAD_CANCEL: 2,
        /////////////////////////////////////////////////////////////////////
        // 'ErrorCodeExtended' Property Constants for PINPad
        /////////////////////////////////////////////////////////////////////

        JPOS_EPPAD_BAD_KEY: 201,
        /////////////////////////////////////////////////////////////////////
        // 'CapUPSChargeState' Capability and 'UPSChargeState' Property
        //    Constants
        /////////////////////////////////////////////////////////////////////

        PWR_UPS_FULL: 0x00000001,
        PWR_UPS_WARNING: 0x00000002,
        PWR_UPS_LOW: 0x00000004,
        PWR_UPS_CRITICAL: 0x00000008,
        /////////////////////////////////////////////////////////////////////
        // 'PowerSource' Property Constants
        /////////////////////////////////////////////////////////////////////

        PWR_SOURCE_NA: 1,
        PWR_SOURCE_AC: 2,
        PWR_SOURCE_BATTERY: 3,
        PWR_SOURCE_BACKUP: 4,
        /////////////////////////////////////////////////////////////////////
        // 'restartPOS', 'standbyPOS', 'suspendPOS' Methods:
        //   'reason' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PWR_REASON_REQUEST: 1,
        PWR_REASON_ALLOW: 2,
        PWR_REASON_DENY: 3,
        /////////////////////////////////////////////////////////////////////
        // Status Update Event: 'Status' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PWR_SUE_UPS_FULL: 11,
        PWR_SUE_UPS_WARNING: 12,
        PWR_SUE_UPS_LOW: 13,
        PWR_SUE_UPS_CRITICAL: 14,
        PWR_SUE_FAN_STOPPED: 15,
        PWR_SUE_FAN_RUNNING: 16,
        PWR_SUE_TEMPERATURE_HIGH: 17,
        PWR_SUE_TEMPERATURE_OK: 18,
        PWR_SUE_SHUTDOWN: 19,
        PWR_SUE_BAT_LOW: 20,
        PWR_SUE_BAT_CRITICAL: 21,
        PWR_SUE_BAT_CAPACITY_REMAINING: 22,
        PWR_SUE_RESTART: 23,
        PWR_SUE_STANDBY: 24,
        PWR_SUE_USER_STANDBY: 25,
        PWR_SUE_SUSPEND: 26,
        PWR_SUE_USER_SUSPEND: 27,
        PWR_SUE_PWR_SOURCE: 28,
        //###################################################################
        //#### POS Printer Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // Printer Station Constants
        /////////////////////////////////////////////////////////////////////

        PTR_S_JOURNAL: 1,
        PTR_S_RECEIPT: 2,
        PTR_S_SLIP: 4,
        PTR_S_JOURNAL_RECEIPT: 3,
        PTR_S_JOURNAL_SLIP: 5,
        PTR_S_RECEIPT_SLIP: 6,
        PTR_TWO_RECEIPT_JOURNAL: 0x8003,
        PTR_TWO_SLIP_JOURNAL: 0x8005,
        PTR_TWO_SLIP_RECEIPT: 0x8006,
        /////////////////////////////////////////////////////////////////////
        // 'CapCharacterSet' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_CCS_ALPHA: 1,
        PTR_CCS_ASCII: 998,
        PTR_CCS_KANA: 10,
        PTR_CCS_KANJI: 11,
        PTR_CCS_UNICODE: 997,
        /////////////////////////////////////////////////////////////////////
        // 'CharacterSet' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_CS_UNICODE: 997,
        PTR_CS_ASCII: 998,
        PTR_CS_ANSI: 999,
        /////////////////////////////////////////////////////////////////////
        // 'ErrorLevel' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_EL_NONE: 1,
        PTR_EL_RECOVERABLE: 2,
        PTR_EL_FATAL: 3,
        /////////////////////////////////////////////////////////////////////
        // 'MapMode' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_MM_DOTS: 1,
        PTR_MM_TWIPS: 2,
        PTR_MM_ENGLISH: 3,
        PTR_MM_METRIC: 4,
        /////////////////////////////////////////////////////////////////////
        // 'CapXxxColor' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_COLOR_PRIMARY: 0x00000001,
        PTR_COLOR_CUSTOM1: 0x00000002,
        PTR_COLOR_CUSTOM2: 0x00000004,
        PTR_COLOR_CUSTOM3: 0x00000008,
        PTR_COLOR_CUSTOM4: 0x00000010,
        PTR_COLOR_CUSTOM5: 0x00000020,
        PTR_COLOR_CUSTOM6: 0x00000040,
        PTR_COLOR_CYAN: 0x00000100,
        PTR_COLOR_MAGENTA: 0x00000200,
        PTR_COLOR_YELLOW: 0x00000400,
        PTR_COLOR_FULL: 0x80000000,
        /////////////////////////////////////////////////////////////////////
        // 'CapXxxCartridgeSensor' and  'XxxCartridgeState' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_CART_UNKNOWN: 0x10000000,
        PTR_CART_OK: 0x00000000,
        PTR_CART_REMOVED: 0x00000001,
        PTR_CART_EMPTY: 0x00000002,
        PTR_CART_NEAREND: 0x00000004,
        PTR_CART_CLEANING: 0x00000008,
        /////////////////////////////////////////////////////////////////////
        // 'CartridgeNotify'  Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_CN_DISABLED: 0x00000000,
        PTR_CN_ENABLED: 0x00000001,
        /////////////////////////////////////////////////////////////////////
        // 'PageModeDescriptor'  Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_PM_BITMAP: 0x00000001,
        PTR_PM_BARCODE: 0x00000002,
        PTR_PM_BM_ROTATE: 0x00000004,
        PTR_PM_BC_ROTATE: 0x00000008,
        PTR_PM_OPAQUE: 0x00000010,
        /////////////////////////////////////////////////////////////////////
        // 'PageModePrintDirection'  Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_PD_LEFT_TO_RIGHT: 1,
        PTR_PD_BOTTOM_TO_TOP: 2,
        PTR_PD_RIGHT_TO_LEFT: 3,
        PTR_PD_TOP_TO_BOTTOM: 4,
        /////////////////////////////////////////////////////////////////////
        // 'clearPrintArea' and 'pageModePrint' Method Constant
        /////////////////////////////////////////////////////////////////////

        PTR_PM_PAGE_MODE: 1,
        PTR_PM_PRINT_SAVE: 2,
        PTR_PM_NORMAL: 3,
        PTR_PM_CANCEL: 4,
        /////////////////////////////////////////////////////////////////////
        // 'CutPaper' Method Constant
        /////////////////////////////////////////////////////////////////////

        PTR_CP_FULLCUT: 100,
        /////////////////////////////////////////////////////////////////////
        // 'PrintBarCode' Method Constants:
        /////////////////////////////////////////////////////////////////////

        //   'Alignment' Parameter
        //     Either the distance from the left-most print column to the start
        //     of the bar code, or one of the following:

        PTR_BC_LEFT: -1,
        PTR_BC_CENTER: -2,
        PTR_BC_RIGHT: -3,
        //   'TextPosition' Parameter

        PTR_BC_TEXT_NONE: -11,
        PTR_BC_TEXT_ABOVE: -12,
        PTR_BC_TEXT_BELOW: -13,
        //   'Symbology' Parameter:

        //     One dimensional symbologies
        PTR_BCS_UPCA: 101, // Digits
        PTR_BCS_UPCE: 102, // Digits
        PTR_BCS_JAN8: 103, // : EAN 8
        PTR_BCS_EAN8: 103, // : JAN 8 (added in 1.2)
        PTR_BCS_JAN13: 104, // : EAN 13
        PTR_BCS_EAN13: 104, // : JAN 13 (added in 1.2)
        PTR_BCS_TF: 105, // (Discrete 2 of 5) Digits
        PTR_BCS_ITF: 106, // (Interleaved 2 of 5) Digits
        PTR_BCS_Codabar: 107, // Digits, -, $, :, /, ., +,
        //   4 start/stop characters
        //   (a, b, c, d)
        PTR_BCS_Code39: 108, // Alpha, Digits, Space, -, .,
        //   $, /, +, %, start/stop (*)
        // Also has Full ASCII feature
        PTR_BCS_Code93: 109, // Same characters as Code 39
        PTR_BCS_Code128: 110, // 128 data characters
        //        (The following were added in Release 1.2)
        PTR_BCS_UPCA_S: 111, // UPC-A with supplemental
        //   barcode
        PTR_BCS_UPCE_S: 112, // UPC-E with supplemental
        //   barcode
        PTR_BCS_UPCD1: 113, // UPC-D1
        PTR_BCS_UPCD2: 114, // UPC-D2
        PTR_BCS_UPCD3: 115, // UPC-D3
        PTR_BCS_UPCD4: 116, // UPC-D4
        PTR_BCS_UPCD5: 117, // UPC-D5
        PTR_BCS_EAN8_S: 118, // EAN 8 with supplemental
        //   barcode
        PTR_BCS_EAN13_S: 119, // EAN 13 with supplemental
        //   barcode
        PTR_BCS_EAN128: 120, // EAN 128
        PTR_BCS_OCRA: 121, // OCR 'A'
        PTR_BCS_OCRB: 122, // OCR 'B'

        // Added in Release 1.8
        PTR_BCS_Code128_Parsed: 123,
        // The followings RSS have been deprecated in 1.12. Use the GS1DATABAR constants below instead.
        PTR_BCS_RSS14: 131, // Reduced Space Symbology - 14 digit GTIN
        PTR_BCS_RSS_EXPANDED: 132, // RSS - 14 digit GTIN plus additional fields

        // Added in Release 1.12
        PTR_BCS_GS1DATABAR: 131, // GS1 DataBar Omnidirectional
        PTR_BCS_GS1DATABAR_E: 132, // GS1 DataBar Expanded
        PTR_BCS_GS1DATABAR_S: 133, // GS1 DataBar Stacked Omnidirectional
        PTR_BCS_GS1DATABAR_E_S: 134, // GS1 DataBar Expanded Stacked

        //     Two dimensional symbologies
        PTR_BCS_PDF417: 201,
        PTR_BCS_MAXICODE: 202,
        // Added in Release 1.13
        PTR_BCS_DATAMATRIX: 203, // Data Matrix
        PTR_BCS_QRCODE: 204, // QR Code
        PTR_BCS_UQRCODE: 205, // Micro QR Code
        PTR_BCS_AZTEC: 206, // Aztec
        PTR_BCS_UPDF417: 207, // Micro PDF 417

        //     Start of Printer-Specific bar code symbologies
        PTR_BCS_OTHER: 501,
        /////////////////////////////////////////////////////////////////////
        // 'PrintBitmap' and 'PrintMemoryBitmap' Method Constants:
        /////////////////////////////////////////////////////////////////////

        //   'Width' Parameter
        //     Either bitmap width or:
        PTR_BM_ASIS: -11, // One pixel per printer dot

        //   'Alignment' Parameter
        //     Either the distance from the left-most print column to the start
        //     of the bitmap, or one of the following:
        PTR_BM_LEFT: -1,
        PTR_BM_CENTER: -2,
        PTR_BM_RIGHT: -3,
        //   'Type' Parameter ('PrintMemoryBitmap' only)
        PTR_BMT_BMP: 1,
        PTR_BMT_JPEG: 2,
        PTR_BMT_GIF: 3,
        /////////////////////////////////////////////////////////////////////
        // 'RotatePrint' Method: 'Rotation' Parameter Constants
        // 'RotateSpecial' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_RP_NORMAL: 0x0001,
        PTR_RP_RIGHT90: 0x0101,
        PTR_RP_LEFT90: 0x0102,
        PTR_RP_ROTATE180: 0x0103,
        // Version 1.7. One of the following values can be
        // ORed with one of the above values.
        PTR_RP_BARCODE: 0x1000,
        PTR_RP_BITMAP: 0x2000,
        /////////////////////////////////////////////////////////////////////
        // 'SetLogo' Method: 'Location' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_L_TOP: 1,
        PTR_L_BOTTOM: 2,
        /////////////////////////////////////////////////////////////////////
        // 'TransactionPrint' Method: 'Control' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_TP_TRANSACTION: 11,
        PTR_TP_NORMAL: 12,
        /////////////////////////////////////////////////////////////////////
        // 'MarkFeed' Method: 'Type' Parameter Constants
        // 'CapRecMarkFeed' Property Constants
        /////////////////////////////////////////////////////////////////////

        PTR_MF_TO_TAKEUP: 1,
        PTR_MF_TO_CUTTER: 2,
        PTR_MF_TO_CURRENT_TOF: 4,
        PTR_MF_TO_NEXT_TOF: 8,
        /////////////////////////////////////////////////////////////////////
        // 'ChangePrintSide' Method: 'Side' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_PS_UNKNOWN: 0,
        PTR_PS_SIDE1: 1,
        PTR_PS_SIDE2: 2,
        PTR_PS_OPPOSITE: 3,
        /////////////////////////////////////////////////////////////////////
        // 'drawRuledLine' Method: 'lineDirection' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_RL_HORIZONTAL: 1, // Added in 1.13
        PTR_RL_VERTICAL: 2, // Added in 1.13


        /////////////////////////////////////////////////////////////////////
        // 'drawRuledLine' Method: 'lineStyle' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_LS_SINGLE_SOLID_LINE: 1, // Added in 1.13
        PTR_LS_DOUBLE_SOLID_LINE: 2, // Added in 1.13
        PTR_LS_BROKEN_LINE: 3, // Added in 1.13
        PTR_LS_CHAIN_LINE: 4, // Added in 1.13


        /////////////////////////////////////////////////////////////////////
        // 'StatusUpdateEvent' Event: 'status' Parameter Constants
        /////////////////////////////////////////////////////////////////////

        PTR_SUE_COVER_OPEN: 11,
        PTR_SUE_COVER_OK: 12,
        PTR_SUE_JRN_EMPTY: 21,
        PTR_SUE_JRN_NEAREMPTY: 22,
        PTR_SUE_JRN_PAPEROK: 23,
        PTR_SUE_REC_EMPTY: 24,
        PTR_SUE_REC_NEAREMPTY: 25,
        PTR_SUE_REC_PAPEROK: 26,
        PTR_SUE_SLP_EMPTY: 27,
        PTR_SUE_SLP_NEAREMPTY: 28,
        PTR_SUE_SLP_PAPEROK: 29,
        PTR_SUE_JRN_CARTRIDGE_EMPTY: 41,
        PTR_SUE_JRN_CARTRIDGE_NEAREMPTY: 42,
        PTR_SUE_JRN_HEAD_CLEANING: 43,
        PTR_SUE_JRN_CARTDRIGE_OK: 44,
        PTR_SUE_JRN_CARTRIDGE_OK: 44,
        PTR_SUE_REC_CARTRIDGE_EMPTY: 45,
        PTR_SUE_REC_CARTRIDGE_NEAREMPTY: 46,
        PTR_SUE_REC_HEAD_CLEANING: 47,
        PTR_SUE_REC_CARTDRIGE_OK: 48,
        PTR_SUE_REC_CARTRIDGE_OK: 48,
        PTR_SUE_SLP_CARTRIDGE_EMPTY: 49,
        PTR_SUE_SLP_CARTRIDGE_NEAREMPTY: 50,
        PTR_SUE_SLP_HEAD_CLEANING: 51,
        PTR_SUE_SLP_CARTRIDGE_OK: 52,
        PTR_SUE_IDLE: 1001,
        // Added in Release 1.8
        PTR_SUE_JRN_COVER_OPEN: 60,
        PTR_SUE_JRN_COVER_OK: 61,
        PTR_SUE_REC_COVER_OPEN: 62,
        PTR_SUE_REC_COVER_OK: 63,
        PTR_SUE_SLP_COVER_OPEN: 64,
        PTR_SUE_SLP_COVER_OK: 65,
        /////////////////////////////////////////////////////////////////////
        // 'ResultCodeExtended' Property Constants for Printer
        /////////////////////////////////////////////////////////////////////

        JPOS_EPTR_COVER_OPEN: 201, // (Several)
        JPOS_EPTR_JRN_EMPTY: 202, // (Several)
        JPOS_EPTR_REC_EMPTY: 203, // (Several)
        JPOS_EPTR_SLP_EMPTY: 204, // (Several)
        JPOS_EPTR_SLP_FORM: 205, // EndRemoval
        JPOS_EPTR_TOOBIG: 206, // PrintBitmap
        JPOS_EPTR_BADFORMAT: 207, // PrintBitmap
        JPOS_EPTR_JRN_CARTRIDGE_REMOVED: 208, // (Several)
        JPOS_EPTR_JRN_CARTRIDGE_EMPTY: 209, // (Several)
        JPOS_EPTR_JRN_HEAD_CLEANING: 210, // (Several)
        JPOS_EPTR_REC_CARTRIDGE_REMOVED: 211, // (Several)
        JPOS_EPTR_REC_CARTRIDGE_EMPTY: 212, // (Several)
        JPOS_EPTR_REC_HEAD_CLEANING: 213, // (Several)
        JPOS_EPTR_SLP_CARTRIDGE_REMOVED: 214, // (Several)
        JPOS_EPTR_SLP_CARTRIDGE_EMPTY: 215, // (Several)
        JPOS_EPTR_SLP_HEAD_CLEANING: 216, // (Several)

        //###################################################################
        //#### Scanner Constants
        //###################################################################

        /////////////////////////////////////////////////////////////////////
        // 'ScanDataType' Property Constants
        /////////////////////////////////////////////////////////////////////

        // One dimensional symbologies
        SCAN_SDT_UPCA: 101, // Digits
        SCAN_SDT_UPCE: 102, // Digits
        SCAN_SDT_JAN8: 103, // : EAN 8
        SCAN_SDT_EAN8: 103, // : JAN 8
        SCAN_SDT_JAN13: 104, // : EAN 13
        SCAN_SDT_EAN13: 104, // : JAN 13
        SCAN_SDT_TF: 105, // (Discrete 2 of 5)
        //   Digits
        SCAN_SDT_ITF: 106, // (Interleaved 2 of 5)
        //   Digits
        SCAN_SDT_Codabar: 107, // Digits, -, $, :, /, .,
        //   +, 4 start/stop
        //   characters (a, b, c,
        //   d)
        SCAN_SDT_Code39: 108, // Alpha, Digits, Space,
        //   -, ., $, /, +, %,
        //   start/stop (*)
        // Also has Full Ascii
        //   feature
        SCAN_SDT_Code93: 109, // Same characters as
        //   Code 39
        SCAN_SDT_Code128: 110, // 128 data characters
        SCAN_SDT_UPCA_S: 111, // UPC-A with
        //   supplemental barcode
        SCAN_SDT_UPCE_S: 112, // UPC-E with
        //   supplemental barcode
        SCAN_SDT_UPCD1: 113, // UPC-D1
        SCAN_SDT_UPCD2: 114, // UPC-D2
        SCAN_SDT_UPCD3: 115, // UPC-D3
        SCAN_SDT_UPCD4: 116, // UPC-D4
        SCAN_SDT_UPCD5: 117, // UPC-D5
        SCAN_SDT_EAN8_S: 118, // EAN 8 with
        //   supplemental barcode
        SCAN_SDT_EAN13_S: 119, // EAN 13 with
        //   supplemental barcode
        SCAN_SDT_EAN128: 120, // EAN 128
        SCAN_SDT_OCRA: 121, // OCR 'A'
        SCAN_SDT_OCRB: 122, // OCR 'B'

        // One dimensional symbologies (Added in Release 1.8)
        //        The following RSS constants deprecated in 1.12.
        //        Instead use the GS1DATABAR constants below.
        SCAN_SDT_RSS14: 131, // Reduced Space Symbology - 14 digit GTIN
        SCAN_SDT_RSS_EXPANDED: 132, // RSS - 14 digit GTIN plus additional fields

        // One dimensional symbologies (added in Release 1.12)
        SCAN_SDT_GS1DATABAR: 131, // GS1 DataBar Omnidirectional (normal or stacked)
        SCAN_SDT_GS1DATABAR_E: 132, // GS1 DataBar Expanded (normal or stacked)

        // Composite Symbologies (Added in Release 1.8)
        SCAN_SDT_CCA: 151, // Composite Component A.
        SCAN_SDT_CCB: 152, // Composite Component B.
        SCAN_SDT_CCC: 153, // Composite Component C.

        // Two dimensional symbologies
        SCAN_SDT_PDF417: 201,
        SCAN_SDT_MAXICODE: 202,
        //  - One dimensional symbologies (added in 1.11)
        SCAN_SDT_DATAMATRIX: 203, // Data Matrix
        SCAN_SDT_QRCODE: 204, // QR Code
        SCAN_SDT_UQRCODE: 205, // Micro QR Code
        SCAN_SDT_AZTEC: 206, // Aztec
        SCAN_SDT_UPDF417: 207, // Micro PDF 417

        // Special cases
        SCAN_SDT_OTHER: 501, // Start of Scanner-
        //   Specific bar code
        //   symbologies
        SCAN_SDT_UNKNOWN: 0   // Cannot determine the

    }
});