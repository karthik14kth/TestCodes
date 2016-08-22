//x port status - red for edge
//x label edge - color
//x autolayout after add
//x ip for nodes
//2 mode: real-time, and history(slider) 

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.Viewer.CloseFramePolicy;
import org.graphstream.ui.swingViewer.Viewer.ThreadingModel;
import org.graphstream.ui.swingViewer.util.DefaultShortcutManager;

public class GraphViewer implements Runnable {
	public static void main(String... args) {
		String hostAddr = "localhost";
	    if (args.length > 0)
	    	hostAddr = args[0];
		SwingUtilities.invokeLater(new GraphViewer(hostAddr));
	}

	Graph graph;
	Viewer viewer;
	View view;
	Thread treader;
	GraphReader reader;
	SpringBox layout;
	Vector<byte[]> fileHistory = new Vector<byte[]>();
	JSlider sliderHistory = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);

	public GraphViewer(String dirName)
	{
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		graph = new MultiGraph("");
		layout = new SpringBox(false, new Random(0));
		reader = new GraphReader(graph, this, dirName);
		
	}
	
	public void run() {
		if (!SwingUtilities.isEventDispatchThread())
			throw new RuntimeException("Not in SWING thread");
        
		treader = new Thread(reader);
		treader.start();
		
		init();
	}

	public void nodesAdded()
	{
		layout.shake();
	}
	
	public void addNewBuf(byte[] buf)
	{
		fileHistory.add(buf);
		sliderHistory.setMaximum(fileHistory.size() - 1);
	}

	
	private void readDefaultCSS()
	{
		try {
			File file = new File("default.css");
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			fis.read(data);
			fis.close();
			String s = new String(data, "UTF-8");
			graph.addAttribute("ui.stylesheet", s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		
		readDefaultCSS();
		/*
		 * Initialize the GUI part.
		 */
		viewer = new Viewer(graph, ThreadingModel.GRAPH_IN_SWING_THREAD);
        viewer.enableAutoLayout(layout);
		view = viewer.addDefaultView(false);

		viewer.setCloseFramePolicy(CloseFramePolicy.EXIT);
		//view.setMouseManager(new InternalMouseManager());
		//view.setShortcutManager(new InternalShortcutManager());

		// Create GUI frame:  panel with a button and the GraphStream view
		buttonAndGraphGui();
	}

	private void buttonAndGraphGui() {
		JPanel toolbar = new JPanel();
		
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		JButton buttonStart = new JButton("Real-Time");
		buttonStart.addActionListener (new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				treader.resume();
				sliderHistory.setEnabled(false);
			}
		});
		JButton buttonStop = new JButton("History");
		buttonStop.addActionListener (new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if (fileHistory.size() > 1)
				{
					treader.suspend();
					sliderHistory.setEnabled(true);
				}
			}
		});
		
		sliderHistory.addChangeListener (new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				if (!sliderHistory.isEnabled())
					return;
				JSlider source = (JSlider)event.getSource();
		        if (!source.getValueIsAdjusting()) {
		            byte[] buf = fileHistory.get((int)source.getValue());
		            		            
	    			graph.clear();
	    			readDefaultCSS();
	    			Graph tmpGraph = new MultiGraph("");
	    			GraphReader.readBuf(buf, tmpGraph);
	    			GraphReader.mergeGraphs(graph, tmpGraph);
		        }
			}
		});
		sliderHistory.setEnabled(false);
		toolbar.add(buttonStart);
		toolbar.add(buttonStop);
		toolbar.add(sliderHistory);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(view);
		panel.add(toolbar);

		JFrame frame = new javax.swing.JFrame("GraphViewer");
		frame.getContentPane().add(panel);
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/*class InternalMouseManager extends DefaultMouseManager {
		protected void mouseButtonPressOnElement(GraphicElement element,
				MouseEvent event) {
			super.mouseButtonPressOnElement(element, event);

			JOptionPane.showMessageDialog(view,
					"You click on " + element.getId());
		}
	}*/

	class InternalShortcutManager extends DefaultShortcutManager {
		public void keyTyped(KeyEvent event) {
			super.keyTyped(event);

			switch (event.getKeyChar()) {
			case 'S':
			case 's':

				layout.compute();

				break;
			default:
			}
		}
	}


}