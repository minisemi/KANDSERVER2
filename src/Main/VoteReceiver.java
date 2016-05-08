package Main;

/**
 * Created by Alexander on 2016-05-06.
 */
public class VoteReceiver {
    public String receiverIP;
    public int receiverPort;

    public void setReceiverIP (String receiverIP){
        this.receiverIP = receiverIP;
    }

    public void setReceiverPort (int receiverPort){
        this.receiverPort = receiverPort;
    }

    public String getReceiverIP () {return receiverIP;}

    public int getReceiverPort () {return receiverPort;}

}

