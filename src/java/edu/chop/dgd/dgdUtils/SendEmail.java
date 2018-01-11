package edu.chop.dgd.dgdUtils;

/**
 * Created by jayaramanp on 1/8/18.
 */
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail {

    public static void main(String args[]){
        try{
            String host = "smtp.gmail.com";
            String from = "CHOPdgdbfx@gmail.com";
            String pass = "nhohdzukafrkidzf";
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", from);
            props.put("mail.smtp.password", pass);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");

            String[] to = args[0].split(";");

            Session session = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i=0; i < to.length; i++ ) { // changed from a while loop
                toAddress[i] = new InternetAddress(to[i]);
            }
            System.out.println(Message.RecipientType.TO);

            for( int i=0; i < toAddress.length; i++) { // changed from a while loop
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }
            message.setSubject("Welcome to AnthOligo");
            //message.setText("running job");
            message.setText(args[1]);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch(Exception e){
            e.getMessage();
        }
    }
}
