package inputOutput;

import Jama.Matrix;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import org.medcare.igtl.messages.ImageMessage;
import org.medcare.igtl.network.GenericIGTLinkClient;
import org.medcare.igtl.network.IOpenIgtPacketListener;
import org.medcare.igtl.util.Status;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * this class creates a network connection
 *
 * @author
 */

public class Networkconnection extends Thread implements IOpenIgtPacketListener {

    private boolean exit = true;
    private boolean stop = true;

    private GenericIGTLinkClient client;
    private List<ToolData> toolDataList = Collections.synchronizedList(new LinkedList<ToolData>());

    public static void parseXMLStringMessage(String msg) {
        // TODO Auto-generated method stub
    }

    /**
     * @param ipAddress is the hostname of server to be connected to
     * @param port is the port number of server
     */
    public void connect(String ipAddress, int port) {

        exit = true;

        String msg = "<Command Name=\"SomeCommandName\" SomeAttribute1=\"attribute value 1\" SomeAttribute2=\"123\"><Param name=\"Param1\"/><Param name=\"Param2\"/></Command>";

        Networkconnection.parseXMLStringMessage(msg);

        try {
            Log.enableDebugPrint(false);
            Log.enableSystemPrint(false);

            Log.debug("Starting client");
            client = new GenericIGTLinkClient(ipAddress, port);

            client.addIOpenIgtOnPacket(this);
            toolDataList = Collections.synchronizedList(new LinkedList<>());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onRxTransform(String name, TransformNR t) {
        Log.debug("Received Transform: " + t);

        if (exit && stop) {

            boolean foundTool = false;

            synchronized (toolDataList) {
                for (ToolData d : toolDataList) {
                    if (d.name.equals(name)) {
                        d.t = t;
                        foundTool = true;
                    }
                }

                if (!foundTool) {
                    ToolData newData = new ToolData();
                    newData.name = name;
                    newData.t = t;
                    toolDataList.add(newData);
                }
            }


        } else if (exit == false) {
            client.stopClient();
        }

        if (name.equals("RegistrationTransform") || name.equals("CALIBRATION")) {
            // System.err.println("Received Registration Transform");
            Log.debug("Setting fiducial registration matrix: " + t);
            return;
        } else if (name.equals("TARGET")) {
            // System.err.println("Received RAS Transform: TARGET");
            Log.debug("Setting task space pose: " + t);

        } else if (name.equals("myTransform")) {
            // System.err.println("Received Transformation Matrix: myTransform");
            Log.debug("Setting task space pose: " + t);

        } else {
            // System.err.println("Received unidentified transform matrix");
            Log.debug("Setting task space pose: " + t);
        }
    }


    public void setExit(boolean value) {
        exit = value;
    }

    public void setBreak(boolean value) {
        stop = value;
    }

    public void run() {
        //Connection();
    }

    @Override
    public TransformNR getTxTransform(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status onGetStatus(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onRxString(String name, String body) {
    }

    @Override
    public String onTxString(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onRxDataArray(String name, Matrix data) {
        // TODO Auto-generated method stub

    }

    @Override
    public double[] onTxDataArray(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onRxImage(String name, ImageMessage image) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTxNDArray(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRxNDArray(String name, float[] data) {
        // TODO Auto-generated method stub
        Log.debug("Name" + name);
        for (int i = 0; i < data.length; i++) {
            Log.debug("Data[" + i + "]=" + data[i]);
        }
    }

    public List<ToolData> getToolDataList() {
        return toolDataList;
    }

    public class ToolData {
        String name;
        TransformNR t;
    }
}
