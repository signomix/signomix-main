package com.signomix.util;


import java.util.Base64;

import com.signomix.common.HexTool;

/**
 *
 * @author greg
 */
public class TestDownlink {
    
    public static void main(String[] args){
        System.out.println(Base64.getEncoder().encodeToString(HexTool.hexStringToByteArray(args[0])));
    }
    
}
