package ioio.examples.hello;


import java.util.ArrayList;

import ioio.examples.hello.R;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

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
	private EditText runningLedCountEditText;
	private int LED_COUNT = 23;

	private void connectSeekBarAndEditText(final SeekBar seekbar, final EditText editText){
		editText.setText(""+seekbar.getProgress());
		editText.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (editText.getText().length() > 0) {
					seekbar.setProgress(Integer.parseInt(editText.getText().toString()));
				}
				else {
					seekbar.setProgress(0);
				}
				return false;
			}
		});

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser){
					editText.setText(""+seekBar.getProgress());
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "LED Test");
		menu.add(0, 1, 1,"Input Test");
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
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
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
		runningLedCountSeekBar.setProgress(5);
		runningLedCountEditText = (EditText) findViewById(R.id.ledCountEditText);

		connectSeekBarAndEditText(runningLedCountSeekBar, runningLedCountEditText);

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
		private ArrayList<DigitalOutput> myOwnLeds;
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
			myOwnLeds = new ArrayList<DigitalOutput>(48);
			for (int i = 2; i <= 48; i++) {
				DigitalInput input = ioio_.openDigitalInput(i);
				try {
					if (input.read()) {
						input.close();
						myOwnLeds.add(ioio_.openDigitalOutput(i,true));
						Toast.makeText(MainActivity.this, "pin "+i+" true", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(MainActivity.this, "pin "+i+" false", Toast.LENGTH_SHORT).show();
						input.close();
					}
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
				} catch (InterruptedException e) {
					Toast.makeText(MainActivity.this, "problem with pin "+i, Toast.LENGTH_SHORT).show();
					input.close();
				} 

			}
			LED_COUNT = myOwnLeds.toArray().length;
			ledCountSeekBar.setMax(LED_COUNT);
			Toast.makeText(MainActivity.this, "found "+ ledCountSeekBar.getMax()+ "LEDs", Toast.LENGTH_SHORT).show();
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
			if (LED_COUNT == 0) {
				return;
			}
			//for (DigitalOutput led : myOwnLeds) {
			//	led.write(!button2_.isChecked());
			//}
			int numOfLeds = runningLedCountSeekBar.getProgress();
			if (numOfLeds == 0) {
				return;
			}
			myOwnLeds.get(currentLED-runningLedCountSeekBar.getProgress() % LED_COUNT).write(true);
			if(button3_.isChecked()){
				currentLED++;
				if(currentLED >= ledCountSeekBar.getProgress()){
					currentLED = 0;
				}
			}else {
				currentLED--;
				if(currentLED < 0 ){
					currentLED = ledCountSeekBar.getProgress()-1;
				}
			}


			myOwnLeds.get(currentLED).write(false);
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