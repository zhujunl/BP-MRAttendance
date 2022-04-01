package com.miaxis.attendance.service;

/**
 * @author Admin
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class MxResponseCode {

    public static int CODE_DEFAULT = Integer.MIN_VALUE;


    public static int CODE_SUCCESS = 0;
    public static String MSG_SUCCESS = "success";


    public static int CODE_ILLEGAL_PARAMETER = -1;
    public static String MSG_ILLEGAL_PARAMETER = "illegal parameter";


    public static int CODE_OPERATION_FAILED = 2;
    public static String MSG_OPERATION_FAILED = "some operations failed";

    public static int CODE_OPERATION_ERROR = -2;
    public static String MSG_OPERATION_ERROR = "operation failed";


    public static int Code_Illegal_Image_Face = -101;
    public static String Msg_Illegal_Image_Face = "illegal face image";


    public static int Code_Illegal_Image_Finger = -102;
    public static String Msg_Illegal_Image_Finger = "illegal finger image";




}
