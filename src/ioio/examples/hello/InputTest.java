package ioio.examples.hello;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.widget.ToggleButton;

public class InputTest extends AbstractIOIOActivity {
	private ToggleButton button_;
	private Handler uiThreadCallback;

	

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statustest);
		button_ = (ToggleButton) findViewById(R.id.button1);
		button_.setClickable(false);
		uiThreadCallback = new Handler();
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/** The on-board LED. */
		private DigitalInput input_;


		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			input_ = ioio_.openDigitalInput(2);
			uiThreadCallback.post(new Runnable() {
				public void run() {
						Toast.makeText(InputTest.this, "input_ is:\"" + input_.toString() + "\"", Toast.LENGTH_LONG).show();
				}
			});
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		protected void loop() throws ConnectionLostException {
			
				uiThreadCallback.post(new Runnable() {
					public void run() {
						try {
						button_.setChecked(!input_.read());
						} catch (ConnectionLostException e) {
							Toast.makeText(InputTest.this, "ConnectionLostException while reading pin 2", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						} catch (InterruptedException e) {
							Toast.makeText(InputTest.this, "InterruptedException while reading pin 2", Toast.LENGTH_SHORT).show();
						}
					}
				});
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}
}
