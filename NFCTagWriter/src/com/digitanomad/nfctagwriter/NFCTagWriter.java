package com.digitanomad.nfctagwriter;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class NFCTagWriter extends Activity {

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_URI = 2;

	EditText messageInput;
	TextView messageOutput;
	TextView tagOutput;

	RadioGroup rgroup01;
	RadioButton rbutton01;
	RadioButton rbutton02;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfctag_writer);

		Button writeBtn = (Button) findViewById(R.id.writeBtn);
		writeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = messageInput.getText().toString();
				int type = TYPE_TEXT;
				if (rbutton02.isChecked()) {
					type = TYPE_URI;
				}

				NdefMessage mMessage = createTagMessage(msg, type);
				byte[] messageBytes = mMessage.toByteArray();

				String hexStr = ConvertUtil.bytesToHex0x(messageBytes);
				messageOutput.setText(hexStr);
				showTag(mMessage);
			}
		});

		messageInput = (EditText) findViewById(R.id.messageInput);
		messageOutput = (TextView) findViewById(R.id.messageOutput);
		tagOutput = (TextView) findViewById(R.id.tagOutput);

		rgroup01 = (RadioGroup) findViewById(R.id.rgroup01);
		rbutton01 = (RadioButton) findViewById(R.id.rbutton01);
		rbutton02 = (RadioButton) findViewById(R.id.rbutton02);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nfctag_writer, menu);
		return true;
	}

	private NdefMessage createTagMessage(String msg, int type) {
		NdefRecord[] records = new NdefRecord[1];

		if (type == TYPE_TEXT) {
			records[0] = createTextRecord(msg, Locale.KOREAN, true);
		} else if (type == TYPE_URI) {
			records[0] = createUriRecord(msg.getBytes());
		}

		// NdefMessage 생성자는 NdefRecord 배열 객체를 파라미터로 전달받는다.
		NdefMessage mMessage = new NdefMessage(records);
		return mMessage;
	}

	private NdefRecord createTextRecord(String text, Locale locale,
			boolean encodeInUtf8) {
		final byte[] langBytes = locale.getLanguage().getBytes(
				Charsets.US_ASCII);
		final Charset utfEncoding = encodeInUtf8 ? Charsets.UTF_8 : Charset
				.forName("UTF-16");
		final byte[] textBytes = text.getBytes(utfEncoding);
		final int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		final char status = (char) (utfBit + langBytes.length);
		final byte[] data = Bytes.concat(new byte[] { (byte) status },
				langBytes, textBytes);

		// NdefRecord의 생성자의 첫 번째 파라미터는 3bit로 표현되는 TNF 값. 잘 알려진 MIME 타입으로 지정
		// 두 번째 파라미터는 구체적인 데이터 타입으로 NdefRecord.RTD_TEXT의 경우 MIME 타입 중에
		// 'text/plain'과 같다.
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

	private NdefRecord createUriRecord(byte[] data) {
		return new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI,
				new byte[0], data);
	}

	// 태그 메시지 객체를 파싱하는 코드
	private void showTag(NdefMessage mMessage) {
		List<ParsedRecord> records = NdefMessageParser.parse(mMessage);
		final int size = records.size();
		tagOutput.setText("");

		for (int i = 0; i < size; i++) {
			ParsedRecord record = records.get(i);

			int recordType = record.getType();
			String recordStr = "";
			if (recordType == ParsedRecord.TYPE_TEXT) {
				recordStr = "TEXT: " + ((TextRecord) record).getText() + "\n";
			} else if (recordType == ParsedRecord.TYPE_URI) {
				recordStr = "URI: " + ((UriRecord) record).getUri().toString()
						+ "\n";
			}

			tagOutput.append(recordStr);
			tagOutput.invalidate();
		}
	}

}
