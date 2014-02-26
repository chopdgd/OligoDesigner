package edu.chop.dgd.primer;

import nu.xom.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by jayaramanp on 2/14/14.
 */
public class AmpliconXomAnalyzer{

    public void initRecord(String name){
    // just start a new record
        System.out.println("Start processing amplicon record:" + name);
}



    public String parseRecord(String inputString) throws ParsingException, IOException {

        Reader reader = new StringReader(inputString);
        Document doc = new Builder().build(reader);

        System.out.println("here is the element node:\t" + doc.getRootElement().getLocalName());
        Element mNode = doc.getRootElement();

        if(mNode.getLocalName().equals("DASDNA")){
            Elements nodeChildren = mNode.getChildElements();
            for(int c=0; c<nodeChildren.size(); c++){
                if(nodeChildren.get(c).getLocalName().equals("SEQUENCE")){
                    Element node = nodeChildren.get(c);
                    String seqId = node.getAttributeValue("id");
                    String seqStart = node.getAttributeValue("start");
                    String seqStop = node.getAttributeValue("stop");

                    Elements childNodes = node.getChildElements();
                    for(int i=0; i<childNodes.size(); i++){
                        Element childNode = childNodes.get(i);
                        System.out.println("the element local name is:"+childNode.getLocalName());
                        if(childNode.getLocalName().equals("DNA")){
                            String sequence = childNode.getValue();
                            System.out.println("here is the sequence:"+sequence);
                            return sequence;
                        }
                    }
                }
            }
        }
        //System.out.println("number of Downloaded PubmedIds: " + downloadedPubmed);
        //System.out.println("number of dois: " + doiNumberCount);
        return null;

    }

    public Element parseSubrecord(Element node) {

        System.out.println("here is the element node:\t" + node.getLocalName());

        if(node.getLocalName().equals("DNA")){
            String sequence = node.getValue();
            System.out.println("here is the sequence:"+sequence);
            return node;
        }
        //System.out.println("number of Downloaded PubmedIds: " + downloadedPubmed);
        //System.out.println("number of dois: " + doiNumberCount);
        return null;

    }

}
