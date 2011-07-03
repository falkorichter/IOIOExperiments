package ioio.examples.hello;


import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
	private ToggleButton button3_;
	private SeekBar frequencySeekBar;
	private EditText frequencyEditText;
	private SeekBar  ledCountSeekBar;
	private EditText ledCountEditText;
	private SeekBar  runningLedCountSeekBar;
	private EditText runningLedCountEditText;;
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
		button3_ = (ToggleButton) findViewById(R.id.button3);
		frequencySeekBar = (SeekBar) findViewById(R.id.frequencySeekBar);
		frequencySeekBar.setMax(1000);
		frequencySeekBar.setProgress(500);
		frequencyEditText = (EditText) findViewById(R.id.frequencyEditText);
		
		
		connectSeekBarAndEditText(frequencySeekBar, frequencyEditText);
		
		ledCountSeekBar = (SeekBar) findViewById(R.id.ledCountSeekBar);
		ledCountSeekBar.setMax(LED_COUNT);
		ledCountSeekBar.setProgress(5);
		ledCountEditText = (EditText) findViewById(R.id.ledCountEditText);
		
		connectSeekBarAndEditText(ledCountSeekBar, ledCountEditText);
		
		
		
		runningLedCountSeekBar = (SeekBar) findViewById(R.id.runningLedCountSeekBar);
		runningLedCountSeekBar.setMax(LED_COUNT);
		runningLedCountSeekBar.setProgress(1);
		runningLedCountEditText = (EditText) findViewById(R.id.runningLedCountEditText);

		connectSeekBarAndEditText(runningLedCountSeekBar, runningLedCountEditText);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 2, 0,"Runnig Detection");
		menu.add(0, 0, 1, "LED Test");
		menu.add(0, 1, 2,"Input Test");
		menu.add(0, 1, 3,"PWM Test");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent(this, StatusTest.class);
			startActivity(intent);
			break;
		case 1:
			Intent intent1 = new Intent(this, InputTest.class);
			startActivity(intent1);
		case 2:
			Intent intent2 = new Intent(this, BetterRunningLights.class);
			startActivity(intent2);
		case 3:
			Intent intent3 = new Intent(this, PwmTestActivity.class);
			startActivity(intent3);
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
			
			if(button3_.isChecked()){
				int oldLED = currentLED - runningLedCountSeekBar.getProgress();
				oldLED = oldLED % ledCountSeekBar.getProgress();
				myOwnLeds[oldLED].write(true);
				currentLED++;
				if(currentLED >= ledCountSeekBar.getProgress()){
					currentLED = 0;
				}
			}else {
				int oldLED = currentLED + runningLedCountSeekBar.getProgress();
				oldLED = oldLED % ledCountSeekBar.getProgress();
				myOwnLeds[oldLED].write(true);
				currentLED--;
				if(currentLED < 0 ){
					currentLED = ledCountSeekBar.getProgress()-1;
				}
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