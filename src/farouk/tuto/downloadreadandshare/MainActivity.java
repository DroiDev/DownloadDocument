package farouk.tuto.downloadreadandshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.download).setOnClickListener(download);
		findViewById(R.id.get_pict_preview).setOnClickListener(preview);
	}

	OnClickListener download = new OnClickListener() {

		@Override
		public void onClick(View v) {
			EditText url_input = (EditText) findViewById(R.id.url_to_download);
			String[] url = url_input.getText().toString().split("/");
			String title = url[url.length - 1];
			DownloadFile d = new DownloadFile(MainActivity.this, title);
			d.execute(url_input.getText().toString());
		}
	};

	OnClickListener preview = new OnClickListener() {

		@Override
		public void onClick(View v) {
			EditText url_input = (EditText) findViewById(R.id.url_to_download);
			ImagePreview d = new ImagePreview();
			d.execute(url_input.getText().toString());
		}
	};

	public class ImagePreview extends AsyncTask<String, Integer, Object> {

		public ImagePreview() {
			super();
		}

		@Override
		protected final Bitmap doInBackground(final String... urlFile) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(
						urlFile[0]).getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected final void onPostExecute(final Object result) {
			super.onPostExecute(result);
			ImageView photo = (ImageView) findViewById(R.id.image_preview);
			if (result != null)
				photo.setImageBitmap((Bitmap) result);
			else
				Toast.makeText(getApplicationContext(), R.string.download_fail,
						Toast.LENGTH_LONG).show();
		}
	}
}
