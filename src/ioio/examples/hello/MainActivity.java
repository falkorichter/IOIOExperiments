package ioio.examples.hello;

import java.sql.Array;

import ioio.examples.hello.R;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link AbstractIOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends AbstractIOIOActivity {
	private ToggleButton button_;
	private ToggleButton button2_;
	private SeekBar frequencySeekBar;
	private SeekBar  ledCountSeekBar;
	private static final int LED_COUNT = 23;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button1);
		button2_ = (ToggleButton) findViewById(R.id.button2);
		frequencySeekBar = (SeekBar) findViewById(R.id.frequencySeekBar);
		frequencySeekBar.setMax(1000);
		frequencySeekBar.setProgress(500);
		ledCountSeekBar = (SeekBar) findViewById(R.id.ledCountSeekBar);
		ledCountSeekBar.setMax(LED_COUNT);
		ledCountSeekBar.setProgress(5);

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
		private DigitalOutput led_;
		private DigitalOutput myOwnLed_;
		private DigitalOutput[] myOwnLeds;
		private int currentLED;
		

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
			currentLED = 0;
			led_ = ioio_.openDigitalOutput(0, true);
			myOwnLed_ = ioio_.openDigitalOutput(1, true);
			myOwnLeds = new DigitalOutput[LED_COUNT];
			for (int i = 0; i < myOwnLeds.length; i++) {
				DigitalOutput led = myOwnLeds[i];
				led = ioio_.openDigitalOutput((i*2)+3);
				myOwnLeds[i] = led;
			}
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
			led_.write(!button_.isChecked());
			myOwnLed_.write(!button2_.isChecked());
			//for (DigitalOutput led : myOwnLeds) {
			//	led.write(!button2_.isChecked());
			//}
			myOwnLeds[currentLED].write(true);
			currentLED++;
			if(currentLED >= ledCountSeekBar.getProgress()){
				currentLED = 0;
			}
			myOwnLeds[currentLED].write(false);
			try {
				sleep(frequencySeekBar.getProgress());
			} catch (InterruptedException e) {
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