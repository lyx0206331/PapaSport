package android.zyapi;



import java.io.ByteArrayOutputStream;

public class Conversion {
	/* 
	 * 16进制数字字符�? 
	 */ 
	private static String hexString="0123456789ABCDEF";
	/* 
	 * 将字符串编码�?16进制数字,适用于所有字符（包括中文�? 
	 */ 
	public static String encode(String str)
	{ 
		//根据默认编码获取字节数组 
		byte[] bytes=str.getBytes(); 
		StringBuilder sb=new StringBuilder(bytes.length*2);
		//将字节数组中每个字节拆解�?2�?16进制整数 
		for(int i=0;i<bytes.length;i++) 
		{ 
			sb.append(hexString.charAt((bytes[i]&0xf0)>>4)); 
			sb.append(hexString.charAt((bytes[i]&0x0f)>>0)); 
		} 
		return sb.toString(); 
	} 


	/* 
	 * �?16进制数字解码成字符串,适用于所有字符（包括中文�? 
	 */ 
	public static String decode(String bytes)
	{ 
		ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
		//将每2�?16进制整数组装成一个字�? 
		for(int i=0;i<bytes.length();i+=2) 
			baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1)))); 
		return new String(baos.toByteArray());
	} 
	/**  
	 * 将两个ASCII字符合成�?个字节；  
	 * 如："EF"--> 0xEF  
	 * @param src0 byte  
	 * @param src1 byte  
	 * @return byte  
	 */  
	public static byte uniteBytes(byte src0, byte src1) {   
		byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
		_b0 = (byte)(_b0 << 4);   
		byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
		byte ret = (byte)(_b0 ^ _b1);   
		return ret;   
	}    
	/**  
	 * 将指定字符串src，以每两个字符分割转换为16进制形式  
	 * 如："2B44EFD9" �?> byte[]{0x2B, 0×44, 0xEF, 0xD9}  
	 * @param src String  
	 * @return byte[]  
	 */  
	public static byte[] HexString2Bytes(String src){
		byte[] ret = new byte[src.length()/2];   
		byte[] tmp = src.getBytes();   
		for(int i=0; i< tmp.length/2; i++){   
			ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);   
		}   
		return ret;   
	}   

	/**  
	 * 将指定byte数组�?16进制的形式打印到控制�?  
	 * @param hint String  
	 * @param b byte[]  
	 * @return void  
	 */  
	public static void printHexString(String hint, byte[] b) {
		System.out.print(hint);
		for (int i = 0; i < b.length; i++) {   
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {   
				hex = '0' + hex;   
			}   
			System.out.print(hex.toUpperCase() + " ");
		}   
		System.out.println("");
	}   

	/**  
	 *  将byte数组转换成16进制
	 * @param b byte[]  
	 * @return String  
	 */  
	public static String Bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {   
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {   
				hex = '0' + hex;   
			}   
			ret += hex.toUpperCase();   
		}   
		return ret;   
	}  


	public static String oneBytes2HexString(byte b) {
		String ret = "";
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {   
			hex = '0' + hex;   
		}   
		ret += hex.toUpperCase();     
		return ret;   
	}



	/** 
	 * @功能: BCD码转�?10进制�?(阿拉伯数�?) 
	 * @参数: BCD�? 
	 * @结果: 10进制�? 
	 */  
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {  
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));  
			temp.append((byte) (bytes[i] & 0x0f));  
		}  
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp  
				.toString().substring(1) : temp.toString();  
	}  

	 //�ַ�����ת��Ϊ16�����ַ�
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        char[] buffer = new char[2];  
        for (int i = 0; i < src.length; i++) {  
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            //System.out.println(buffer); 
            stringBuilder.append(buffer);  
        }  
        return stringBuilder.toString();  
    }
}

