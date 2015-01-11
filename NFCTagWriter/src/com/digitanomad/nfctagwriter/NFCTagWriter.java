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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	public static final int REQ_CODE_PUSH = 1001;
	public static final int SHOW_PUSH_CONFIRM = 2001;

	EditText messageInput;
	TextView messageOutput;
	TextView tagOutput;

	RadioGroup rgroup01;
	RadioButton rbutton01;
	RadioButton rbutton02;
	
	NdefMessage mMessage;

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
		mMessage = new NdefMessage(records);
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
		
		showDialog(SHOW_PUSH_CONFIRM);
	}

	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case SHOW_PUSH_CONFIRM:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("NFC 태그 전송");
			builder.setMessage("NFC 태그를 전송하시겠습니까?");
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent newIntent = new Intent(getApplicationContext(), NFCTagPush.class);
					newIntent.putExtra("tag", mMessage);
					startActivityForResult(newIntent, REQ_CODE_PUSH);
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
			
			return builder.create();

		default:
			break;
		}
		
		return null;
	}
}
