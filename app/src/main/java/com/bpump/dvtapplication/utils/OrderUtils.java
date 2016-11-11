package com.bpump.dvtapplication.utils;

/**
 * 作者：Create on 2016/11/10 10:31  by  qinli
 * 邮箱：
 * 描述：TODO 蓝牙指令工具类
 * 最近修改：2016/11/10 10:31 modify by qinli
 */

public class OrderUtils {

    //注释为不用修改，可直接使用  /***/注释为需要根据传入的参数对校验和进行重新校验

    //结束当前操作(发送长度为0)
    public static byte FINISH_NOW_OPERATION[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x30, 0x31, (byte) 0xCD, 0x16};

    /**
     * 读取治疗记录（示例是最后一条日志记录的情况）(发送长度为2，记录编号(2bytes))
     */
    public static byte READ_TREAMENT_RECORD[] = new byte[]{0x68, 0x02, 0x00, 0x04,
            0x00, 0x31, 0x31, 0x05, 0x00, (byte) 0xD5, 0x16};

    /**
     * 选择下载的治疗记录(发送长度为2，记录编号(2bytes))
     */
    public static byte DOWNLOAD_TREAMENT_RECORD[] = new byte[]{0x68, 0x02, 0x00, 0x04,
            0x00, 0x32, 0x31, 0x01, 0x00, (byte) 0xD2, 0x16};

    //读取当前日志信息(发送长度为0)
    public static byte READ_NOW_RECORD[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x33, 0x31, (byte) 0xD0, 0x16};

    //读取下一条日志信息(发送长度为0)
    public static byte READ_NEXT_RECORD[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x34, 0x31, (byte) 0xD1, 0x16};

    /**
     * 读取用户信息(初始化用户编号=2) (发送长度为1，发送用户编号)
     */
    public static byte READ_USERINFORMATION[] = new byte[]{0x68, 0x02, 0x00, 0x03,
            0x00, 0x35, 0x31, 0x02, (byte) 0xD5, 0x16};

    /**
     * 注册用户信息/修改用户信息(发送长度为11，信息包含：用户ID(1byte)、血压信息(2bytes)、每次的最大治疗分钟数(2bytes) 、病历编号(6bytes))
     */
    public static byte REGISTER_OR_UPDATE_USERINFORMATION[] = new byte[]{0x68, 0x02, 0x00, 0x13,
            0x00, 0x36, 0x31, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xD7, 0x16};

    //暂停当前操作(发送长度为0)
    public static byte PAUSE_NOW_OPERATION[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x2F, 0x31, (byte) 0xCC, 0x16};

    //读取当前气压(发送长度为0)
    public static byte READ_NOW_PRESSURE[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x2E, 0x31, (byte) 0xCB, 0x16};

    //静脉回盈时间测量(发送长度为0)
    public static byte VENOUS_RETURN_TIME_MESSURE[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x2D, 0x31, (byte) 0xCA, 0x16};

    //平均压测量(发送长度为0)
    public static byte AVG_PRESSURE_MESSURE[] = new byte[]{0x68, 0x02, 0x00, 0x02,
            0x00, 0x2C, 0x31, (byte) 0xC9, 0x16};

    /**
     * 某个腔泄气到指定气压(发送长度为3 )
     */
    public static byte STALENESS_TO_APPOINT_PRESSURE[] = new byte[]{0x68, 0x02, 0x00, 0x05,
            0x00, 0x2B, 0x31, 0x00, 0x00, 0x00, (byte) 0xCB, 0x16};

    /**
     * 某个腔加压到指定气压(发送长度为3 )
     */
    public static byte PRELUM_TO_APPOINT_PRESSURE[] = new byte[]{0x68, 0x02, 0x00, 0x05,
            0x00, 0x2A, 0x31, 0x00, 0x00, 0x00, (byte) 0xCA, 0x16};

    /**
     * 标定气压 (发送长度为2)
     */
    public static byte DEMARCATE_PRESSURE[] = new byte[]{0x68, 0x03, 0x00, 0x04,
            0x00, 0x28, 0x31, 0x00, 0x00, (byte) 0xC8, 0x16};

    //读取用户信息（输入用户编号）
    public static byte[] CALCULATE_FOR_READ_USER_INFORMATION(int USER_ID) {
        int i;
        byte FINAL_BYTES[] = READ_USERINFORMATION;
        FINAL_BYTES[7] = (byte) USER_ID;
        FINAL_BYTES[8] = 0;
        for (i = 0; i < 8; i++)
            FINAL_BYTES[8] += FINAL_BYTES[i];
        return FINAL_BYTES;
    }
}

