package com.lcf.esensedial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DialBook {
    ArrayList<Dial> dials;

    public DialBook(File file) throws IOException {
        dials = new ArrayList<>();

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
            dials.add(new Dial(line));
        }
    }

    public ArrayList<Dial> getDials() {
        return dials;
    }

    public String searchBook(ArrayList<String> recog_List) {
        for(int i=0;i<dials.size();i++) {
            Dial temp = dials.get(i);

            if (temp.getActs().size() == recog_List.size()) {
                int flag = 0;
                for(int j=0;j<temp.getActs().size();j++) {
                    if (!temp.getActs().get(j).equals(recog_List.get(j))) {
                        flag = 1;
                        break;
                    }
                }
                if(flag == 0) {
                    return temp.getNumber();
                }
            }
        }

        return "NO_FOUND";
    }
}
