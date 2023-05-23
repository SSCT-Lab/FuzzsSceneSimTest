package com.nineya.springboot.common.SSH;

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream{

    OutputStream output1;
    OutputStream output2;

    public MultiOutputStream(OutputStream output1,OutputStream output2){
        this.output1 = output1;
        this.output2 = output2;
    }
    //重写方法，将创建两条输出流
    @Override
    public void write(int b) throws IOException {
        // TODO Auto-generated method stub
        output1.write(b);
        output2.write(b);
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        output1.close();
    }

}
