import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDOT;

public class GraphReader implements Runnable {

	final static byte protocolVersion = 1;
	private Graph graph;
	private String hostAddr;
	private GraphViewer graphViewer;
	protected Socket clientSocket = null;
	protected byte clientid = 0;
	
	public GraphReader(Graph graph, GraphViewer graphViewer, String hostAddr)
	{
		this.graph = graph;
		this.graphViewer = graphViewer;
		this.hostAddr = hostAddr;
	}
	
	public static void readBuf(byte[] buf, Graph gr)
	{
		System.out.println("source: " + new String(buf));
		FileSource fs = new FileSourceDOT();
		fs.addSink(gr);
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
   		try {
			fs.readAll(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean mergeGraphs(Graph graph, Graph tmpGraph)
	{
		boolean added = false; 
		List<Node> nodes = new LinkedList<Node>(); 
	    for (Node node : graph) {
	    	if (tmpGraph.getNode(node.getId()) == null)
	    	{
	    		nodes.add(node);
	    		System.out.println("removing node: " + node.getId());
	    	}
        }
	    
	    for (Node node : tmpGraph) {
	    	if (graph.getNode(node.getId()) == null)
	    	{
	    		Node nn = graph.addNode(node.getId());
	    		for (String attr : node.getAttributeKeySet())
	    			nn.addAttribute(attr, node.getAttribute(attr));
	    		System.out.println("adding node: " + node.getId());
	    		added = true;
	    	}

        }
		
	    List<Edge> edges = new LinkedList<Edge>(); 
	    for (Edge edge : graph.getEdgeSet()) {
	    	if (tmpGraph.getEdge(edge.getId()) == null)
	    	{
	    		System.out.println("removing edge: " + edge.getId());
	    		edges.add(edge);
	    	}
        }

	    for (Edge edge : edges) {
	    	graph.removeEdge(edge);
	    }
	    for (Node node : nodes) {
	    	graph.removeNode(node);
	    }

		
	    for (Edge edge : tmpGraph.getEdgeSet()) {
	    	Edge oe = graph.getEdge(edge.getId());
	    	if (oe == null)
	    	{
	    		Edge newe = graph.addEdge(edge.getId(), edge.getNode0().getId(), edge.getNode1().getId(), edge.isDirected());
	    		System.out.println("adding edge: " + newe.getId());
	    		for (String attr : edge.getAttributeKeySet())
	    			newe.addAttribute(attr, edge.getAttribute(attr));
	    	} else {
	    		Object val = edge.getAttribute("ui.style");
	    		if (val != null)
	    			oe.setAttribute("ui.style", val);
	    		else
	    			oe.removeAttribute("ui.style");
	    	}
        }
	    return added;
	}
	
	 
	private ByteBuffer genGetDataReq(byte clientid, int token)
	{
		final short messageId = 25;
		byte[] buf = (Integer.toString(token)).getBytes();
		ByteBuffer result = ByteBuffer.allocate(12 + buf.length).order(ByteOrder.LITTLE_ENDIAN);
		result.put(protocolVersion);
		result.putShort(messageId);
		result.put(clientid);
		result.putInt(token);
		result.putInt(buf.length);
		result.put(buf);
		
		return result;
	}
	
	private ByteBuffer genIfInitReq()
	{
		final short messageId = 1;
		
		ByteBuffer result = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
		result.put(protocolVersion);
		result.putShort(messageId);
		result.put((byte)0);
		result.putInt(0);
		result.putInt(0);
		return result;
	}
	
	public void run() {
		try {
			clientSocket = new Socket(hostAddr, 32001);

			DataOutputStream outs = new DataOutputStream(
					clientSocket.getOutputStream());
			DataInputStream ins = new DataInputStream(
					clientSocket.getInputStream());
			outs.write(genIfInitReq().array());

			/*if (ver != protocolVersion || msgid != 2)
		{
			System.out.println("Unsupported protocol version");
			clientSocket.close();
			clientSocket = null;
			return;
		}*/
			byte buf[] = null;
			byte lastbuf[] = null;
			boolean first = true;

			while (!Thread.currentThread().isInterrupted() && clientSocket.isConnected()) 
			{
				ByteBuffer header = ByteBuffer.allocate(12);
				header.order(ByteOrder.LITTLE_ENDIAN);
				ins.readFully(header.array());
				header.get(); //int ver = 
				int msgid = header.getShort();
				byte msgclientid = header.get();
				header.getInt(); //int token = 
				int size = header.getInt();

				byte[] data = new byte[size];
				if (size > 0) {
					ins.readFully(data);
				}
				switch(msgid)
				{
				case 2: //ifInitCfm
					clientid = msgclientid;
					outs.write(genGetDataReq(clientid, 5).array());
					break;
				case 23: //reportInd
					if (buf == null)
					{
						buf = data;
					} else {
						byte[] c = new byte[buf.length + data.length];
						System.arraycopy(buf, 0, c, 0, buf.length);
						System.arraycopy(data, 0, c, buf.length, data.length);
						buf = c;
					}
					break;
				case 26: //getDataCfm
					if (first)
					{
						readBuf(buf, graph);    		
						first = false;
					} else
					{
						if (!Arrays.equals(buf, lastbuf))
						{
							graphViewer.addNewBuf(buf);
							System.out.println("new graph");
							Graph tmpGraph = new MultiGraph("");
							
							readBuf(buf, tmpGraph);
							
							if (mergeGraphs(graph, tmpGraph))
								graphViewer.nodesAdded();
						}
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					lastbuf = buf;
					buf = null;   		
					outs.write(genGetDataReq(clientid, 5).array());
					break;
				default:
					System.out.println("Unknown message: " + msgid);
				}

			}
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
	}
	

}
