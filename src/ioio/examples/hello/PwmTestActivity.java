package ioio.examples.hello;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

public class PwmTestActivity extends AbstractIOIOActivity {
	private SeekBar pwmSeekBar;
	private EditText pwmEditText;
	private CheckBox pwmCheckBox;

	

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pwmtest);
		pwmSeekBar = (SeekBar) findViewById(R.id.pwmValue);
		pwmSeekBar.setMax(255);
		pwmEditText = (EditText) findViewById(R.id.pwmEditText);
		connectSeekBarAndEditText(pwmSeekBar, pwmEditText);
		
		pwmCheckBox = (CheckBox) findViewById(R.id.pwmCheckBox);
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
		private PwmOutput output;
		private int frequency = 50;
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
			super.setup();
//			make it go around in circles
			output = ioio_.openPwmOutput(48, frequency);
//			output = ioio_.openPwmOutput(48, 100);
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
			if(pwmCheckBox.isChecked()){
				output.setDutyCycle(1.0f * pwmSeekBar.getProgress() / pwmSeekBar.getMax());
			}
			else {
				int percent = pwmSeekBar.getMax() * frequency	 / pwmSeekBar.getProgress() ;
				output.setPulseWidth(percent);
			}
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
