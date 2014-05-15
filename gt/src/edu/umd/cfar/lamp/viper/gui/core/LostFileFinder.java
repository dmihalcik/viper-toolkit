/*
 * Created on Mar 7, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package edu.umd.cfar.lamp.viper.gui.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * Since the user shouldn't have to deal with the fact that the 
 * viper metadata files and the media files will be stored 
 * independently, the need to have a structured way of locating 
 * the media files becomes important. What is likely necessary is 
 * a user folder with .index files, .project files and the like.
 * How does .ppt handle this? Some lousy hack, no doubt. I really
 * want fs level indexing like bfs, where i can say 'get me the 
 * file with the following hash' or something. Damn, that would be cool,
 * and I have to wait until longhorn comes out, and then I still can't 
 * use it since I'm trapped in the sandbox. Maybe I just complain too
 * much.
 */
public class LostFileFinder {
	private static String pathSepRE = "\\0" + Integer.toOctalString((int) File.pathSeparatorChar);
	private static int countChars(String s, char character) {
		int count = 0;
		char[] cs = s.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			if (character == cs[i]) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Searches all directories specified in the paths variable
	 * for the first occurance of the given fname.
	 * 
	 * @param fname
	 * @param paths
	 * @return File
	 */
	public static File which(String fname, String paths) {
		String[] A = paths.split(pathSepRE);
		File[] toSearch = new File[A.length];
		for (int i = 0; i < A.length; i++) {
			toSearch[i] = new File(A[i]);
		}
		return which(fname, toSearch);
	}

	/**
	 * Searches all directories specified in the paths variable
	 * for the first occurance of the given fname.
	 * 
	 * @param fname
	 * @param paths
	 * @return File
	 */
	public static File which(String fname, File[] paths) {
		for (int i = 0; i < paths.length; i++) {
			File cwd = paths[i];
			File[] ls = cwd.listFiles();
			if (ls != null) {
				for (int j = 0; j < ls.length; j++) {
					File toTest = ls[j];
					if (toTest.getName().equals(fname)) {
						return toTest;
					}
				}
			}
			if (Thread.interrupted()) {
				throw new StoppedFinderException();
			}
		}
		return null;
	}

	/**
	 * Searches all directories specified in the paths variable
	 * for the all occurance of the given fname.
	 * 
	 * @param fname
	 * @param paths
	 * @return list of files
	 */
	public static List whichAll(String fname, File[] paths) {
		List L = new ArrayList();
		for (int i = 0; i < paths.length; i++) {
			File cwd = paths[i];
			File[] ls = cwd.listFiles();
			if (ls != null) {
				for (int j = 0; j < ls.length; j++) {
					File toTest = ls[j];
					if (toTest.getName().equals(fname)) {
						L.add(toTest);
					}
				}
			}
			if (Thread.interrupted()) {
				throw new StoppedFinderException();
			}
		}
		return L;
	}

	/**
	 * If which doesn't work, open up a can of whoop-ass on it.
	 * 
	 * @param oldPath the path of the old file to look for; used
	 * as a heuristic
	 * @param paths the paths to search beneath
	 * @return the found file, or <code>null</code> if none is found
	 * with the given name
	 */
	public static File smartFindLocalPath(String oldPath, File[] paths) {
		int unix = countChars(oldPath, '/');
		int win = countChars(oldPath, '\\');
		int mac = countChars(oldPath, ':');
		String[] reversePath;
		Set op;
		if (unix + win + mac == 0) {
			reversePath = new String[] {oldPath};
			op = Collections.EMPTY_SET;
		} else {
			String regex;
			if (unix > win && unix > mac) {
				regex = "/";
			} if (win > unix && win > mac) {
				regex = "\\\\";
			} else if (mac > unix && mac > win) {
				regex = ":"; 
			} else {
				regex = "[\\\\/:]";
			}
			String[] path = oldPath.split(regex);
			reversePath = new String[path.length];
			op = new HashSet();
			for (int f = 0, b = path.length - 1; f < path.length; f++, b--) {
				reversePath[b] = path[f];
				op.add(path[f]);
			} 
		}
		
		// First search current tree
		File currDir = new File(System.getProperty("user.dir"));
		Set exclude = Collections.EMPTY_SET;
		if (currDir != null && currDir.exists() && currDir.canRead()) {
			File currSearch = smartFindLocalPath(reversePath, op, currDir, exclude);
			if (currSearch != null) {
				return currSearch; 
			}
		}
		
		// then search user tree
		File userDir = new File(System.getProperty("user.home"));
		if (currDir != null) {
			exclude = Collections.singleton(currDir);
		}
		if (userDir != null && userDir.exists() && userDir.canRead()) {
			File currSearch = smartFindLocalPath(reversePath, op, userDir, exclude);
			if (currSearch != null) {
				return currSearch; 
			}
		}
		
		// then search up and up
		File[] fsRoots = File.listRoots();
		exclude = new TreeSet();
		if (userDir != null) {
			exclude.add(userDir);
		}
		if (currDir != null) {
			exclude.add(currDir);
		}
		List reorderedRoots;
		if (exclude.size() > 0) {
			reorderedRoots = new LinkedList();
			for (int i = 0; i < fsRoots.length; i++) {
				File curr = fsRoots[i];
				String currPath = curr.getAbsolutePath();
				boolean early = false;
				if (userDir != null) {
					early = userDir.getAbsolutePath().startsWith(currPath);
				}
				if (!early && currDir != null) {
					early = currDir.getAbsolutePath().startsWith(currPath);
				}
				if (early) {
					reorderedRoots.add(0, curr); 
				} else {
					reorderedRoots.add(curr); 
				}
			}
		} else {
			reorderedRoots = Arrays.asList(fsRoots);
		}
		Iterator iter = reorderedRoots.iterator();
		while (iter.hasNext()) {
			File curr = (File) iter.next(); 
			if (curr.exists() && curr.canRead()) {
				File currSearch = smartFindLocalPath(reversePath, op, curr, exclude);
				if (currSearch != null) {
					return currSearch; 
				}
			}
		}
		return null;
	}

	/**
	 * Gets the default search list: the user directory,
	 * the current directory, then all the roots.
	 * @return Places to look for files
	 */
	public static File[] getDefaultSearchPaths() {
		List l = new LinkedList();
		addAsFileIfNonNull(l, "user.dir");
		addAsFileIfNonNull(l, "user.home");
		File[] roots = File.listRoots();
		l.addAll(Arrays.asList(roots));
		return (File[]) l.toArray(new File[l.size()]);
	}
	/**
	 * Gets the system property value specified, and, if it isn't 
	 * <code>null</code>, adds a new <code>File</code> object for
	 * the value to the list
	 * @param l The list of <code>File</code>s to update, if the 
	 * value is non-<code>null</code>.
	 * @param property The system property to check, ie 
	 * <code>user.home</code>
	 */
	private static void addAsFileIfNonNull(List l, String property) {
		String s = System.getProperty(property);
		if (s != null) {
			l.add(new File(s));
		}
	}

	/**
	 * Looks for the local version of the file, first checking
	 * the name of the file as absolute path, then using 'which', 
	 * then by searching the disk.
	 * 
	 * @param oldPath
	 * @param paths roots and which directories
	 * @return String
	 * @throws StoppedFinderException if the thread is interrupted
	 */
	public static File findTheFile(URI oldPath, File[] paths) {
		File f = null;
		try {
			f = new File(oldPath);
		} catch (IllegalArgumentException iax) {
			f = null;
		}
		if (f != null && f.exists()) {
			return f;
		}
		
		String p = oldPath.getPath();
		String[] opath = p.split("[\\\\/:]");
		String fname = opath[opath.length - 1];
		f = which(fname, paths);
		
		if (f != null) {
			f = smartFindLocalPath(p, paths);
		}
		
		return f;
	}

	/**
	 * Searches from root on down for the first file it finds named
	 * oldPath[0], and returns the absolute path to same. oldPath,
	 * dirs and except are lists or sets of strings or paths that 
	 * hopefully keep the search somewhat focused.
	 * @param oldPath The path we thought the file had, reversed by path, 
	 *    like dns (eg /fs/lamp/something.mpg is {"something.mpg", "lamp", "fs"})
	 * @param dirs A set (probably a HashSet) containing oldPath[1:]
	 *    These are Strings
	 * @param root The current search root. This searches recursively
	 * @param except Don't look at these absolute paths. These are File objects.
	 * @return String The absolute path to the first file named oldPath[0] found
	 *   except those named explicitly in except or those down paths blocked 
	 *   by except. Returns <code>null</code> if not found.
	 */
	public static File smartFindLocalPath(String[] oldPath, Set dirs, File root, Set except){
		// First search for oldPath[0] in wd, then oldPath[1] and follow, then oldPath[2]
		// and so on, then search all directories recursively if not found.
		if (!root.isDirectory() || !root.canRead()) {
			return null;
		}
		File[] children = root.listFiles();
		if (children == null) {
			return null;
		}

		LinkedList searchDirs = new LinkedList();
		for (int i = 0; i < children.length; i++) {
			if (!except.contains(children[i])) {
				if (children[i].isFile()) {
					if (children[i].getName().equals(oldPath[0])) {
						return children[i];
					}
				} else if (dirs.contains (children[i].getName())) {
					searchDirs.add(0,children[i]);
				} else {
					searchDirs.add(children[i]);
				}
			}
		}
		if (Thread.interrupted()) {
			throw new StoppedFinderException();
		}
		for (Iterator iter = searchDirs.iterator(); iter.hasNext(); ) {
			File curr = (File) iter.next();
			File s = smartFindLocalPath (oldPath, dirs, curr, except);
			if (s != null) {
				return s;
			}
		}
		return null;
	}
	
	private static URI notFoundURI;
	static {
		try {
			notFoundURI = new URI("http", "www.example.com", null, null);
		} catch (URISyntaxException e) {
			assert false : e.getLocalizedMessage();
		}
	}
	
	private static class Searcher extends SwingWorker<URI, Void> {
		private URI oldURI;
		private File[] paths;
		private SearchCompleted f;
		private Window container;

		Searcher(URI oldURI, File[] paths, SearchCompleted f) {
			super();
			this.oldURI = oldURI;
			this.paths = paths;
			this.f = f;
		}
		
		@Override
		protected URI doInBackground() {
			try {
				Thread.sleep(1000);
				File f = LostFileFinder.findTheFile(oldURI, paths);
				if (f == null) {
					return notFoundURI;
				} else {
					return f.toURI();
				}
			} catch (StoppedFinderException sfx) {
				return null;
			} catch (InterruptedException e) {
				return null;
			}
		}
		
		
		public void done() {
			URI found;
			try {
				found = this.get();
			} catch (InterruptedException | ExecutionException e) {
				return;
			}
			if (notFoundURI.equals(found)) {
				found = null;
			} else if (container != null) {
				container.setVisible(false);
				container.dispose();
			}
			if (found == null) {
				f.canceled();
			} else {
				f.found(new File(found));
			}
			super.done();
		}

		/**
		 * Gets the containing window.
		 * @return the window
		 */
		public Window getContainer() {
			return container;
		}

		/**
		 * Sets the containing window. This will be removed when the
		 * searcher finds the item in the user's file system.
		 * @param frame the containing search dialog box
		 */
		public void setContainer(Window frame) {
			container = frame;
		}
		
		File[] getPaths() {
			return paths;
		}
	}
	
	/**
	 * Code to run when the search completes or the
	 * user has browsed for a file or cancelled the search.
	 */
	public static interface SearchCompleted {
		/**
		 * Called after the search has been revoked.
		 */
		public void canceled();
		
		/**
		 * Called when a file has been found or selected.
		 * @param f The found file
		 */
		public void found(File f);
	}
	
	
	/**
	 * Gets a 'Browse for file' dialog box, while starting a search for a file
	 * with the same name in the background.
	 *  
	 * @param oldURI The file to look for
	 * @param paths Places to check
	 * @param whenDone Code to run when the file is found or the user 
	 * 			selected it, or hit cancel or it was not found
	 * @param parent The frame to which the dialog should be attached
	 * @return the dialog window
	 */
	public static Window getSearchDialog(URI oldURI, File[] paths, SearchCompleted whenDone, Frame parent) {
		Searcher s = new Searcher(oldURI, paths, whenDone);
		MissingSearchDialog msd = new MissingSearchDialog(parent, s);
		s.setContainer(msd);
		s.execute();
		msd.setModal(false);
		msd.setLocationRelativeTo(parent);
		msd.setVisible(true);
		return msd;
	}

	private static class MissingSearchDialog extends JDialog {
		private JButton browseButton;
		private JButton cancelButton;
		private Searcher searchThread;
		private JOptionPane op;
		
		
		private void closeWindow() {
			searchThread.cancel(true);
			searchThread.setContainer(null);
			setVisible(false);
			dispose();
		}
		
		private ActionListener browseListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				if (searchThread.getPaths().length > 0) {
					chooser.setCurrentDirectory(searchThread.getPaths()[0]);
				}
				int returnVal = chooser.showOpenDialog(MissingSearchDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File foundFile = chooser.getSelectedFile();
					closeWindow();
					searchThread.f.found(foundFile);
				}
			}
		};
		private ActionListener cancelListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeWindow();
			}
		};
		
		MissingSearchDialog(Frame parent, Searcher searchThread) {
			super(parent, true);

			super.setTitle("Cannot Find File");
			String[] msgs = {"Searching for the media file", "(To specify it manually, press 'Browse'.)"};

			browseButton = new JButton("Browse...");
			browseButton.addActionListener(browseListener);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(cancelListener);
			Object[] options = {browseButton, cancelButton};
			op = new JOptionPane(msgs, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);

			super.setContentPane(op);

			super.setDefaultCloseOperation(
				WindowConstants.DO_NOTHING_ON_CLOSE);

			this.searchThread = searchThread;
			
			super.pack();
		}
	}
	
	/**
	 * Thrown when the search thread is interrupted. 
	 */
	public static class StoppedFinderException extends RuntimeException {
		// It would be nice if there were a way to resume the search from
		// the current location. I miss python
		
		/**
		 * Constructs a new finder exception.
		 */
		public StoppedFinderException() {
			super();
		}
		
		/**
		 * Constructs a new finder stopped with the given detail 
		 * message.
		 * @param message the exception detail message
		 */
		public StoppedFinderException(String message) {
			super(message);
		}
		
		/**
		 * Stops the finder, because of a thrown exception.
		 * @param message the detail message
		 * @param cause the exception to wrap
		 */
		public StoppedFinderException(String message, Throwable cause) {
			super(message, cause);
		}
		
		/**
		 * Stops the finder, because of a thrown exception.
		 * @param cause the exception to wrap
		 */
		public StoppedFinderException(Throwable cause) {
			super(cause);
		}
	}
}
