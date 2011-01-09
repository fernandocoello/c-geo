package carnero.cgeo;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class cgMapImg {
	private cgSettings settings = null;
	private String geocode = null;
	private int level = 1;

	public cgMapImg(cgSettings settingsIn, String geocodeIn) {
		geocode = geocodeIn;
		settings = settingsIn;
	}

	public void setLevel(int levelIn) {
		level = levelIn;
	}

	public void getDrawable(String url) {
		String dirName;
		String fileName;

		if (url == null || url.length() == 0) {
			return;
		}

		if (geocode != null && geocode.length() > 0) {
			dirName = settings.getStorage() + geocode + "/";
			fileName = settings.getStorage() + geocode + "/map_" + level;
		} else {
			return;
		}

		File dir = null;
		dir = new File(settings.getStorage());
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		dir = new File(dirName);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		dir = null;

		HttpClient client = null;
		HttpGet getMethod = null;
		HttpResponse httpResponse = null;
		HttpEntity entity = null;
		BufferedHttpEntity bufferedEntity = null;

		boolean ok = false;

		for (int i = 0; i < 3; i ++) {
			if (i > 0) Log.w(cgSettings.tag, "cgMapImg.getDrawable: Failed to download data, retrying. Attempt #" + (i + 1));

			try {
				client = new DefaultHttpClient();
				getMethod = new HttpGet(url);
				httpResponse = client.execute(getMethod);
				entity = httpResponse.getEntity();
				bufferedEntity = new BufferedHttpEntity(entity);

				Log.i(cgSettings.tag, "[" + entity.getContentLength() + "B] Downloading static map " + url);

				if (bufferedEntity != null) {
					InputStream is = (InputStream)bufferedEntity.getContent();
					FileOutputStream fos = new FileOutputStream(fileName);

					try {
						byte[] buffer = new byte[4096];
						int l;
						while ((l = is.read(buffer)) != -1) {
							fos.write(buffer, 0, l);
						}
						ok = true;
					} catch (IOException e) {
						Log.e(cgSettings.tag, "cgMapImg.getDrawable (saving to cache): " + e.toString());
					} finally {
						is.close();
						fos.flush();
						fos.close();
					}
				}

				if (ok == true) {
					break;
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgMapImg.getDrawable (downloading from web): " + e.toString());
			}
		}
	}
}
