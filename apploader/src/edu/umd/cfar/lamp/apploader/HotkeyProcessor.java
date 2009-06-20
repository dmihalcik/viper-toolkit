/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.apploader;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;
import java.util.logging.*;

import javax.swing.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import edu.umd.cfar.lamp.viper.util.*;

/**
 * @author davidm
 */
public class HotkeyProcessor implements KeyEventPostProcessor, KeyEventDispatcher {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.apploader");;
	private static class HotkeyBinding {
		URI name;
		Actionator generator;
		Component bean;
		String description;
		HotkeyBinding(URI name, Actionator ax, Component b) {
			this.name = name;
			this.generator = ax;
			this.bean = b;
		}
		/**
		 * Returns a short, debug-print friendly description
		 * of the binding.
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return name + " binds " + generator + " to " + bean;
		}
	}


	private class BindingMap {
		private String stage;
		
		/// Maps KeyStrokes to HotkeyBinding objects
		private Map[] hotkeys;

		/// beans with bean-level hotkeys. Don't even bother checking
		// beans that aren't in this list. Beans are reference-counted.
		private Map beans;
	
		//
		private Map usedHotkeys;

		private StringBuffer bindingSetToHtml(Map bindings, StringBuffer sb) {
			sb.append("<table>\n");
			sb.append("<tr><th>Key Combo</th><th>Description</th></tr>\n");
			List bindingList = new ArrayList(bindings.size());
			for (Iterator iter = bindings.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry curr = (Entry) iter.next();
				List references = (List) curr.getValue();
				
				if (references == null || references.isEmpty()) {
				} else {
					Object k = curr.getKey();
					Iterator binds = references.iterator();
					while (binds.hasNext()) {
						HotkeyBinding b = (HotkeyBinding) binds.next();
						bindingList.add(new Pair(k, b));
					}
				}
			}
			Pair[] orderedBindings = new Pair[bindingList.size()];
			orderedBindings = (Pair[]) bindingList.toArray(orderedBindings);
			Arrays.sort(orderedBindings, new Comparator() {
				public int compare(Object o1, Object o2) {
					Pair a = (Pair) o1; Pair b = (Pair) o2;
					HotkeyBinding aB = (HotkeyBinding) a.getSecond();
					HotkeyBinding bB = (HotkeyBinding) b.getSecond();
					return aB.description.compareTo(bB.description);
				}
			});
			for (int i = 0; i < orderedBindings.length; i++) {
				Pair p = orderedBindings[i];
				KeyStroke stroke = (KeyStroke) p.getFirst();
				HotkeyBinding references = (HotkeyBinding) p.getSecond();
				sb.append("<tr><td>").append(keyStroke2String(stroke));
				while (i < orderedBindings.length-1 && references.equals(orderedBindings[i+1].getSecond())) {
					i++;
					sb.append(", ").append(keyStroke2String(stroke));
				} 
				sb.append("</td><td>");
				sb.append(references.description);
				sb.append("</td></tr>\n");
			}
			sb.append("</table>");
			return sb;
		}
		
		/**
		 * Tests to see if no hotkeys are bound.
		 * @return <code>true</code> iff no hot keys
		 * are bound by this binding map.
		 */
		public boolean isEmpty() {
			return usedHotkeys.isEmpty();
		}
		
		StringBuffer toHtml(StringBuffer sb) {
			if (sb == null) {
				sb = new StringBuffer();
			}
			
			for (int i = 0; i < hotkeys.length; i++) {
				if (!hotkeys[i].isEmpty()) {
					sb.append("<h3>hotkeys:")
					  .append(HOTKEYS.INPUT_TYPES[i].getLocalName())
					  .append("</h3>\n");
					bindingSetToHtml(hotkeys[i], sb);
				}
			}
			
			return sb;
		}
		
		/**
		 * Creates a new binding map for the given stage in
		 * the key binding process.
		 * @param stage the stage in binding that the map will be 
		 * used for; only used in log messages - the HotkeyProcessor
		 * is the thing that actually determines which map to use at
		 * which stage
		 */
		public BindingMap(String stage) {
			this.stage = stage;
			hotkeys = new Map[HOTKEYS.INPUT_TYPES.length];
			for (int i = 0; i < hotkeys.length; i++) {
				hotkeys[i] = new HashMap();
			}
			beans = new HashMap();
			usedHotkeys = new HashMap();
		}

		/**
		 * Adds a new hotkey (or replaces the binding) to the given bean 
		 * with the given type. 
		 * @param ax The action to invoke when the hotkey is pressed (in the 
		 *  context defined by the bean and type).
		 * @param bean the Component with the key binding
		 * @param type the type, e.g. input map type (window, has focus, contains focus)
		 * @param key  the key press
		 * @param description text description of the action the binding performs
		 */
		public void addActionator(
			Actionator ax,
			Component bean,
			int type,
			KeyStroke key,
			String description) {
			HotkeyBinding binding = new HotkeyBinding(null, ax, bean);
			binding.description = description;
			if (hotkeys[type].containsKey(key)) {
				Collection c = (Collection) hotkeys[type].get(key);
				c.add(binding);
			} else {
				LinkedList ll = new LinkedList();
				ll.add(binding);
				hotkeys[type].put(key, ll);
			}
			incrementRefs(bean, beans);
			incrementRefs(key, usedHotkeys);
		}
		/**
		 * Tries binding the given biding on the bean.
		 * @param binding the binding
		 * @param bean the bean to check
		 * @return <code>true</code> if binding matches bean and is keystroke is captured
		 */
		private boolean attemptBinding(HotkeyBinding binding, Object bean) {
			if (binding.bean.equals(bean)) {
				int id = ActionEvent.ACTION_PERFORMED;
				String command = binding.generator.getCommand();
				ActionEvent e = new ActionEvent(this, id, command);
				binding.generator.actionPerformed(e);
				return binding.generator.isConsumer();
			}
			return false;
		}

		/**
		 * Tries all the bindings on the given bean.
		 * @param bindings a collection of bindings to try
		 * @param bean the bean to try them on
		 * @return <code>true</code> if a binding was successful
		 */
		public boolean attemptOnAll(Collection bindings, Object bean) {
			Iterator biter = bindings.iterator();
			while (biter.hasNext()) {
				if (attemptBinding((HotkeyBinding) biter.next(), bean)) {
					return true;
				}
			}
			return false;
		}

		private boolean tryBindingsAtLevel(
			int level,
			KeyStroke stroke,
			Component bean) {
			if (beans.containsKey(bean)) {
				Collection bindings = (Collection) hotkeys[level].get(stroke);
				if (null != bindings) {
					if (attemptOnAll(bindings, bean)) {
						return true;
					}
				}
			}
			return false;
		}
		private boolean tryBindingsAtWindow(Window w, KeyStroke stroke) {
			int level = JComponent.WHEN_IN_FOCUSED_WINDOW;
			Collection bindings = (Collection) hotkeys[level].get(stroke);
			if (null != bindings) {
				Iterator biter = bindings.iterator();
				while (biter.hasNext()) {
					HotkeyBinding binding = (HotkeyBinding) biter.next();
					Object toplevel;
					if (binding.bean instanceof JComponent) {
						JComponent widget = (JComponent) binding.bean;
						toplevel = widget.getTopLevelAncestor();
					} else if (binding.bean instanceof JFrame) {
						toplevel = (JFrame) binding.bean;
					} else {
						// XXX - should be swing component (ie either a 
						// jcomponent or jframe
						toplevel = binding.bean;
					}
					
					if (toplevel.equals(w)) {
						int id = ActionEvent.ACTION_PERFORMED;
						String command = binding.generator.getCommand();
						ActionEvent e = new ActionEvent(this, id, command);
						binding.generator.actionPerformed(e);
						if (binding.generator.isConsumer()) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		/**
		 * Process the key stroke, sending out the appropriate 
		 * events to the different bindings.
		 * @param stroke the stroke to process
		 * @return if a binding was found
		 */
		public boolean processStroke(KeyStroke stroke) {
			if (!usedHotkeys.containsKey(stroke)) {
				logger.fine("No " + stage + " binding for: '" + keyStroke2String(stroke) + "'");
				return false;
			}
			logger.fine("Found " + stage + " binding for: '" + keyStroke2String(stroke) + "'");
			Component focal = kfm.getFocusOwner();
			if (focal != null) {
				// Only bother if this is in the same application context
				if (tryBindingsAtLevel(JComponent.WHEN_FOCUSED, stroke, focal)) {
					logger.fine("Binding found at current focus " + stage + " for '" + keyStroke2String(stroke) + "'");
					return true;
				}
				while (focal != null) {
					if (tryBindingsAtLevel(JComponent
						.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						stroke,
						focal)) {
						logger.fine("Binding found at ancestor " + stage + " for '" + keyStroke2String(stroke) + "'");
						return true;
					}
					focal = focal.getParent();
				}
			}
			// Now try window
			Window w = kfm.getActiveWindow();
			if (null != w) {
				if (tryBindingsAtWindow(w, stroke)) {
					logger.fine("Binding found at window " + stage + " for '" + keyStroke2String(stroke) + "'");
					return true;
				}
			}

			logger.fine ("No binding found in current " + stage + " focus for '" + keyStroke2String(stroke) + "'");
			return false;
		}
	}

	private static void incrementRefs(Object target, Map countMap) {
		Integer count = (Integer) countMap.get(target);
		if (null == count) {
			countMap.put(target, new Integer(1));
		} else {
			countMap.put(target, new Integer(count.intValue()+1));
		}
	}
	private static void decrementRefs(Object target, Map countMap) {
		// TODO: use this when writing 'remove binding' methods
		Integer count = (Integer) countMap.get(target);
		if (null != count) {
			int v = count.intValue();
			if (v == 1) {
				countMap.remove(target);
			} else {
				countMap.put(target, new Integer(v-1));
			}
		}
	}

	/**
	 * Get the text representation of the given key code.
	 * @param keyCode the key code
	 * @return the text representation of the key code, e.g. '9' or 'COMMA'
	 */
	public static String getKeyText(int keyCode) {
		if ((keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)
			|| (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)) {
			return String.valueOf((char) keyCode);
		}

		switch (keyCode) {
			case KeyEvent.VK_COMMA :
				return "COMMA";
			case KeyEvent.VK_PERIOD :
				return "PERIOD";
			case KeyEvent.VK_SLASH :
				return "SLASH";
			case KeyEvent.VK_SEMICOLON :
				return "SEMICOLON";
			case KeyEvent.VK_EQUALS :
				return "EQUALS";
			case KeyEvent.VK_OPEN_BRACKET :
				return "OPEN_BRACKET";
			case KeyEvent.VK_BACK_SLASH :
				return "BACK_SLASH";
			case KeyEvent.VK_CLOSE_BRACKET :
				return "CLOSE_BRACKET";

			case KeyEvent.VK_ENTER :
				return "ENTER";
			case KeyEvent.VK_BACK_SPACE :
				return "BACK_SPACE";
			case KeyEvent.VK_TAB :
				return "TAB";
			case KeyEvent.VK_CANCEL :
				return "CANCEL";
			case KeyEvent.VK_CLEAR :
				return "CLEAR";
			case KeyEvent.VK_SHIFT :
				return "SHIFT";
			case KeyEvent.VK_CONTROL :
				return "CONTROL";
			case KeyEvent.VK_ALT :
				return "ALT";
			case KeyEvent.VK_PAUSE :
				return "PAUSE";
			case KeyEvent.VK_CAPS_LOCK :
				return "CAPS_LOCK";
			case KeyEvent.VK_ESCAPE :
				return "ESCAPE";
			case KeyEvent.VK_SPACE :
				return "SPACE";
			case KeyEvent.VK_PAGE_UP :
				return "PAGE_UP";
			case KeyEvent.VK_PAGE_DOWN :
				return "PAGE_DOWN";
			case KeyEvent.VK_END :
				return "END";
			case KeyEvent.VK_HOME :
				return "HOME";
			case KeyEvent.VK_LEFT :
				return "LEFT";
			case KeyEvent.VK_UP :
				return "UP";
			case KeyEvent.VK_RIGHT :
				return "RIGHT";
			case KeyEvent.VK_DOWN :
				return "DOWN";

				// numpad numeric keys handled below
			case KeyEvent.VK_MULTIPLY :
				return "MULTIPLY";
			case KeyEvent.VK_ADD :
				return "ADD";
			case KeyEvent.VK_SEPARATOR :
				return "SEPARATOR";
			case KeyEvent.VK_SUBTRACT :
				return "SUBTRACT";
			case KeyEvent.VK_DECIMAL :
				return "DECIMAL";
			case KeyEvent.VK_DIVIDE :
				return "DIVIDE";
			case KeyEvent.VK_DELETE :
				return "DELETE";
			case KeyEvent.VK_NUM_LOCK :
				return "NUM_LOCK";
			case KeyEvent.VK_SCROLL_LOCK :
				return "SCROLL_LOCK";

			case KeyEvent.VK_F1 :
				return "F1";
			case KeyEvent.VK_F2 :
				return "F2";
			case KeyEvent.VK_F3 :
				return "F3";
			case KeyEvent.VK_F4 :
				return "F4";
			case KeyEvent.VK_F5 :
				return "F5";
			case KeyEvent.VK_F6 :
				return "F6";
			case KeyEvent.VK_F7 :
				return "F7";
			case KeyEvent.VK_F8 :
				return "F8";
			case KeyEvent.VK_F9 :
				return "F9";
			case KeyEvent.VK_F10 :
				return "F10";
			case KeyEvent.VK_F11 :
				return "F11";
			case KeyEvent.VK_F12 :
				return "F12";
			case KeyEvent.VK_F13 :
				return "F13";
			case KeyEvent.VK_F14 :
				return "F14";
			case KeyEvent.VK_F15 :
				return "F15";
			case KeyEvent.VK_F16 :
				return "F16";
			case KeyEvent.VK_F17 :
				return "F17";
			case KeyEvent.VK_F18 :
				return "F18";
			case KeyEvent.VK_F19 :
				return "F19";
			case KeyEvent.VK_F20 :
				return "F20";
			case KeyEvent.VK_F21 :
				return "F21";
			case KeyEvent.VK_F22 :
				return "F22";
			case KeyEvent.VK_F23 :
				return "F23";
			case KeyEvent.VK_F24 :
				return "F24";

			case KeyEvent.VK_PRINTSCREEN :
				return "PRINTSCREEN";
			case KeyEvent.VK_INSERT :
				return "INSERT";
			case KeyEvent.VK_HELP :
				return "HELP";
			case KeyEvent.VK_META :
				return "META";
			case KeyEvent.VK_BACK_QUOTE :
				return "BACK_QUOTE";
			case KeyEvent.VK_QUOTE :
				return "QUOTE";

			case KeyEvent.VK_KP_UP :
				return "KP_UP";
			case KeyEvent.VK_KP_DOWN :
				return "KP_DOWN";
			case KeyEvent.VK_KP_LEFT :
				return "KP_LEFT";
			case KeyEvent.VK_KP_RIGHT :
				return "KP_RIGHT";

			case KeyEvent.VK_DEAD_GRAVE :
				return "DEAD_GRAVE";
			case KeyEvent.VK_DEAD_ACUTE :
				return "DEAD_ACUTE";
			case KeyEvent.VK_DEAD_CIRCUMFLEX :
				return "DEAD_CIRCUMFLEX";
			case KeyEvent.VK_DEAD_TILDE :
				return "DEAD_TILDE";
			case KeyEvent.VK_DEAD_MACRON :
				return "DEAD_MACRON";
			case KeyEvent.VK_DEAD_BREVE :
				return "DEAD_BREVE";
			case KeyEvent.VK_DEAD_ABOVEDOT :
				return "DEAD_ABOVEDOT";
			case KeyEvent.VK_DEAD_DIAERESIS :
				return "DEAD_DIAERESIS";
			case KeyEvent.VK_DEAD_ABOVERING :
				return "DEAD_ABOVERING";
			case KeyEvent.VK_DEAD_DOUBLEACUTE :
				return "DEAD_DOUBLEACUTE";
			case KeyEvent.VK_DEAD_CARON :
				return "DEAD_CARON";
			case KeyEvent.VK_DEAD_CEDILLA :
				return "DEAD_CEDILLA";
			case KeyEvent.VK_DEAD_OGONEK :
				return "DEAD_OGONEK";
			case KeyEvent.VK_DEAD_IOTA :
				return "DEAD_IOTA";
			case KeyEvent.VK_DEAD_VOICED_SOUND :
				return "DEAD_VOICED_SOUND";
			case KeyEvent.VK_DEAD_SEMIVOICED_SOUND :
				return "DEAD_SEMIVOICED_SOUND";

			case KeyEvent.VK_AMPERSAND :
				return "AMPERSAND";
			case KeyEvent.VK_ASTERISK :
				return "ASTERISK";
			case KeyEvent.VK_QUOTEDBL :
				return "QUOTEDBL";
			case KeyEvent.VK_LESS :
				return "LESS";
			case KeyEvent.VK_GREATER :
				return "GREATER";
			case KeyEvent.VK_BRACELEFT :
				return "BRACELEFT";
			case KeyEvent.VK_BRACERIGHT :
				return "BRACERIGHT";
			case KeyEvent.VK_AT :
				return "AT";
			case KeyEvent.VK_COLON :
				return "COLON";
			case KeyEvent.VK_CIRCUMFLEX :
				return "CIRCUMFLEX";
			case KeyEvent.VK_DOLLAR :
				return "DOLLAR";
			case KeyEvent.VK_EURO_SIGN :
				return "EURO_SIGN";
			case KeyEvent.VK_EXCLAMATION_MARK :
				return "EXCLAMATION_MARK";
			case KeyEvent.VK_INVERTED_EXCLAMATION_MARK :
				return "INVERTED_EXCLAMATION_MARK";
			case KeyEvent.VK_LEFT_PARENTHESIS :
				return "LEFT_PARENTHESIS";
			case KeyEvent.VK_NUMBER_SIGN :
				return "NUMBER_SIGN";
			case KeyEvent.VK_MINUS :
				return "MINUS";
			case KeyEvent.VK_PLUS :
				return "PLUS";
			case KeyEvent.VK_RIGHT_PARENTHESIS :
				return "RIGHT_PARENTHESIS";
			case KeyEvent.VK_UNDERSCORE :
				return "UNDERSCORE";

			case KeyEvent.VK_FINAL :
				return "FINAL";
			case KeyEvent.VK_CONVERT :
				return "CONVERT";
			case KeyEvent.VK_NONCONVERT :
				return "NONCONVERT";
			case KeyEvent.VK_ACCEPT :
				return "ACCEPT";
			case KeyEvent.VK_MODECHANGE :
				return "MODECHANGE";
			case KeyEvent.VK_KANA :
				return "KANA";
			case KeyEvent.VK_KANJI :
				return "KANJI";
			case KeyEvent.VK_ALPHANUMERIC :
				return "ALPHANUMERIC";
			case KeyEvent.VK_KATAKANA :
				return "KATAKANA";
			case KeyEvent.VK_HIRAGANA :
				return "HIRAGANA";
			case KeyEvent.VK_FULL_WIDTH :
				return "FULL_WIDTH";
			case KeyEvent.VK_HALF_WIDTH :
				return "HALF_WIDTH";
			case KeyEvent.VK_ROMAN_CHARACTERS :
				return "ROMAN_CHARACTERS";
			case KeyEvent.VK_ALL_CANDIDATES :
				return "ALL_CANDIDATES";
			case KeyEvent.VK_PREVIOUS_CANDIDATE :
				return "PREVIOUS_CANDIDATE";
			case KeyEvent.VK_CODE_INPUT :
				return "CODE_INPUT";
			case KeyEvent.VK_JAPANESE_KATAKANA :
				return "JAPANESE_KATAKANA";
			case KeyEvent.VK_JAPANESE_HIRAGANA :
				return "JAPANESE_HIRAGANA";
			case KeyEvent.VK_JAPANESE_ROMAN :
				return "JAPANESE_ROMAN";
			case KeyEvent.VK_KANA_LOCK :
				return "KANA_LOCK";
			case KeyEvent.VK_INPUT_METHOD_ON_OFF :
				return "INPUT_METHOD_ON_OFF";

			case KeyEvent.VK_AGAIN :
				return "AGAIN";
			case KeyEvent.VK_UNDO :
				return "UNDO";
			case KeyEvent.VK_COPY :
				return "COPY";
			case KeyEvent.VK_PASTE :
				return "PASTE";
			case KeyEvent.VK_CUT :
				return "CUT";
			case KeyEvent.VK_FIND :
				return "FIND";
			case KeyEvent.VK_PROPS :
				return "PROPS";
			case KeyEvent.VK_STOP :
				return "STOP";

			case KeyEvent.VK_COMPOSE :
				return "COMPOSE";
			case KeyEvent.VK_ALT_GRAPH :
				return "ALT_GRAPH";
		}

		if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
			char c = (char) (keyCode - KeyEvent.VK_NUMPAD0 + '0');
			return "NUMPAD" + c;
		}

		return "unknown(0x" + Integer.toString(keyCode, 16) + ")";
	}

	/**
	 * Converts a keystroke into a format parsable by the KeyStroke
	 * constructor. This was as described in the <a 
	 * href="http://javaalmanac.com/egs/javax.swing/Key2Str.html">Java Almanac</a>
	 * @param key the stroke to output as a String
	 * @return the String description of the given stroke
	 */
	public static String keyStroke2String(KeyStroke key) {
		StringBuffer s = new StringBuffer(50);
		int m = key.getModifiers();

		if ((m & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0) {
			s.append("shift ");
		}
		if ((m & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0) {
			s.append("ctrl ");
		}
		if ((m & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0) {
			s.append("meta ");
		}
		if ((m & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) {
			s.append("alt ");
		}
		if ((m & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON1_MASK))
			!= 0) {
			s.append("button1 ");
		}
		if ((m & (InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON2_MASK))
			!= 0) {
			s.append("button2 ");
		}
		if ((m & (InputEvent.BUTTON3_DOWN_MASK | InputEvent.BUTTON3_MASK))
			!= 0) {
			s.append("button3 ");
		}

		switch (key.getKeyEventType()) {
			case KeyEvent.KEY_TYPED :
				char c = key.getKeyChar();
				if (Character.isISOControl(c)) {
					s.append("typed 0x");
					s.append(Integer.toHexString(c));
					s.append(" ");
				} else if (Character.isWhitespace(c)) {
					s.append("typed 0x");
					s.append(Integer.toHexString(c));
					s.append(" ");
				} else {
					s.append("typed ");
					s.append(c);
					s.append(" ");
				}
				break;
			case KeyEvent.KEY_PRESSED :
				s.append("pressed ");
				s.append(getKeyText(key.getKeyCode()) + " ");
				break;
			case KeyEvent.KEY_RELEASED :
				s.append("released ");
				s.append(getKeyText(key.getKeyCode()) + " ");
				break;
			default :
				s.append("unknown-event-type ");
				break;
		}

		return s.toString();
	}

	/**
	 * Converts the key event into its corresponding
	 * key stroke object.
	 * @param e the event
	 * @return the corresponding stroke
	 */
	public static String event2stroke(KeyEvent e) {
		KeyStroke stroke;
		stroke = KeyStroke.getKeyStrokeForEvent(e);
		return keyStroke2String(stroke);
	}


	private AppLoader core;
	private KeyboardFocusManager kfm;
	private BindingMap dispatchBindings;
	private BindingMap postBindings;

	/**
	 * Creates a new, empty hotkey processor.
	 */
	public HotkeyProcessor() {
		kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		dispatchBindings = new BindingMap("dispatch");
		postBindings = new BindingMap("post");
	}
	

	private boolean apply(KeyEvent e, BindingMap m) {
		KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
		int mods = stroke.getModifiers();
		mods = mods & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.META_MASK | InputEvent.ALT_MASK);
		KeyStroke stroke2;
		if (stroke.getKeyEventType() == KeyEvent.KEY_TYPED) {
			stroke2 = KeyStroke.getKeyStroke(new Character(stroke.getKeyChar()), mods);
		} else {
			stroke2 = KeyStroke.getKeyStroke(stroke.getKeyCode(), mods, stroke.isOnKeyRelease());
		}
		return m.processStroke(stroke2);
	}

	/**
	 * Use the key as a hotkey, if possible. Currently doesn't do
	 * ancestorInputAction properly, only works for bubbling upwards.
	 * I'm not sure if I understand the spec on how to do it. 
	 * @param e the event to process
	 * @return true, if the bean is consumed
	 */
	public boolean postProcessKeyEvent(KeyEvent e) {
		if (e.isConsumed()) {
			logger.fine("Consumed before post: '" + event2stroke(e) + "'");
			return false;
		}
		return apply(e, this.postBindings);
	}

	/**
	 * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
	 */
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.isConsumed()) {
			logger.fine("Consumed before dispatch: '" + event2stroke(e) + "'");
			return false;
		}
		return apply(e, this.dispatchBindings);
	}


	/**
	 * Adds a new hotkey (or replaces the binding) to the given bean 
	 * with the given type. 
	 * @param ax The action to invoke when the hotkey is pressed (in the 
	 *  context defined by the bean and type).
	 * @param bean The Component with the key binding
	 * @param type The type, e.g. input map type (window, has focus, contains focus)
	 * @param key  The key press
	 * @param when indicates if the key should be processed in the pre-stage or the post-stage
	 * @param description text description of the action the binding performs
	 */
	public void addActionator(
		Actionator ax,
		Component bean,
		int type,
		KeyStroke key,
		Resource when,
		String description) {
		if (when.equals(HOTKEYS.DuringDispatch)) {
			dispatchBindings.addActionator(ax, bean, type, key, description);
		} else if (when.equals(HOTKEYS.DuringPost)) {
			postBindings.addActionator(ax, bean, type, key, description);
		} else {
			logger.warning ("Not a binding time: " + when);
		}
	}


	/**
	 * Creates a hotkey for the given inputAction statement 
	 * <code>[ a lal:Bean] hotkeys:inputAction [a hotkeys:HotkeyBinding] .</code>
	 * @param binding adds the given binding to the list of hotkey bindings
	 */
	public void addActionator(Statement binding) {
		Resource componentR = binding.getSubject();
		Property p = binding.getPredicate();
		Resource inputActionR = binding.getResource();
		String keyS =
			inputActionR.getProperty(HOTKEYS.hotkey).getString();
		KeyStroke hotkey = HotkeyProcessor.parseKeyTyped(keyS);
		logger.fine("Binding event to '" + keyStroke2String(hotkey) + "'");
		if (null == hotkey) {
			logger.warning(
				"Cannot convert '" + keyS + "' to a hotkey");
			return;
		}
		Resource actionR =
			inputActionR
				.getProperty(HOTKEYS.hasAction)
				.getResource();
		Actionator a = core.getActionForResource(actionR);

		Resource whenR = HOTKEYS.DuringPost;
		if (inputActionR.hasProperty(HOTKEYS.when)) {
			whenR = inputActionR.getProperty(HOTKEYS.when).getResource();
		}
		
		String description = null;
		if (inputActionR.hasProperty(DC_11.description)) {
			description = core.getPrefs().getLocalizedString(inputActionR, DC_11.description);
		} else {
			description = a.getDescription();
		}
		
		int type = HOTKEYS.getTypeIDForInputActionPredicate(p);
		Component component = (Component) core.getLoadedBeanForResource(componentR);
		addActionator(a, component, type, hotkey, whenR, description);
	}
	/**
	 * @return AppLoader
	 */
	public AppLoader getCore() {
		return core;
	}

	/**
	 * Sets the core.
	 * @param core The core to set
	 */
	public void setCore(AppLoader core) {
		this.core = core;
	}

	private static Map modifiers = new HashMap();
	static { // initialize modifier map
		modifiers.put("ctrl", new Integer(InputEvent.CTRL_MASK));
		modifiers.put("control", new Integer(InputEvent.CTRL_MASK));
		modifiers.put("alt", new Integer(InputEvent.ALT_MASK));
		modifiers.put("meta", new Integer(InputEvent.META_MASK));
		modifiers.put("shift", new Integer(InputEvent.SHIFT_MASK));
		modifiers.put("shortcut", new Integer(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	/**
	 * Converts the given string into the corresponding keystroke.
	 * This format is more homogenous and cross-platform than the standard
	 * java way of doing things.
	 * @param s a keystroke string
	 * @return the keystroke
	 */
	public static KeyStroke parseKeyTyped(String s) {
		char c = 0;
		int keyCode = 0;
		int mods = 0;
		Iterator st = StringHelp.debackslashedTokenizer(s);
		boolean pressed = false;
		try {
			String curr = ((String) st.next()).toLowerCase();
			if ("pressed".equals(curr)) {
				pressed = true;
				curr = ((String) st.next()).toLowerCase();
			}
			while (st.hasNext()) { // while there is still one left
				Integer mod = (Integer) modifiers.get(curr);
				if (null == mod) {
					throw new IllegalArgumentException(s + " [" + curr + "]");
				}
				mods |= mod.intValue();
				curr = ((String) st.next()).toLowerCase();
			}
			if (pressed) {
				try {
					keyCode = KeyEvent.class.getField("VK_" + curr.toUpperCase()).getInt(KeyEvent.class);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(s + " [" + curr + "]");
				} catch (NoSuchFieldException e) {
					throw new IllegalArgumentException(s + " [" + curr + "]");
				}
			} else if (curr.length() == 1) {
				c = curr.charAt(0);
			} else {
				throw new IllegalArgumentException(s + " [" + curr + "]");
			}
		} catch(NoSuchElementException nsex) {
			throw new IllegalArgumentException(s);
		}
		KeyStroke ks;
		if (pressed) {
			ks = KeyStroke.getKeyStroke(keyCode, mods);
		} else {
			ks = KeyStroke.getKeyStroke(new Character(c), mods);
		}
		return ks;
	}
	
	/**
	 * Gets an html listing of all the bindings. This is 
	 * just an html fragment; it doesn't include html or 
	 * body tags. 
	 * @return an html fragment describing the bindings
	 */
	public String toHtml() {
		StringBuffer sb = new StringBuffer();
		sb.append("<h1>Hotkey Bindings</h1>\n");
		boolean someFound = false;
		if (!dispatchBindings.isEmpty()) {
			sb.append("<h2>Dispatch Bindings</h2>\n");
			dispatchBindings.toHtml(sb);
			someFound = true;
		}
		if (!postBindings.isEmpty()) {
			sb.append("<h2>Post Bindings</h2>\n");
			postBindings.toHtml(sb);
			someFound = true;
		}
		if (!someFound) {
			sb.append("<p>No hotkeys are bound.</p>");
		}
		return sb.toString();
	}
}
