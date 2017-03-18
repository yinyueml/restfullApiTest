package com.ilawApiTest.common;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by shaowei on 2017/3/16.
 */
public class TextFileParse {

    public static String getJsonStringFromText(File path) throws IOException {
        FileInputStream in=null;
        String jsonStr=null;
        try{
            in=new FileInputStream(path);
            jsonStr= IOUtils.toString(in,"UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            in.close();
        }
        return jsonStr;
    }
}
