package defineoutside.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CreateConnectionToMainframe {
    static Socket connectToMainframe(String host, Integer port, String name) throws IOException {
        Socket s = new Socket(host, port);
        DataInputStream dis = new DataInputStream(s.getInputStream());

        String msg = dis.readUTF();

        if (msg.equals("CRva7SfCPaiBrS7cZh6bNXuupO0qnfOYrgOCZceQFWcFjbiksI1mgcUyhO31AZtz10k6Kj8Ji5XQ0pMObC2BXEKg2XptcVjFdGf")) {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            dos.writeUTF("4v2NZ8RTar54k4PoYEsnjxpL0IObNMgediJQP65QwUwmm9hBw1hQCJvxcSo6tIDwiHY2RkYzmVMWIpN8Oe4rrmPxVum2PBwBnL6");
            dos.writeUTF(name);


            return s;
        }
        return null;
    }
}
