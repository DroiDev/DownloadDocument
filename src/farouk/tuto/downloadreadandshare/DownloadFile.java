package farouk.tuto.downloadreadandshare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * T�l�chargement d'un fichier.
 * 
 * @author faf
 * 
 */
public class DownloadFile extends AsyncTask<String, Integer, Object> {
	/**
	 * Le chemin du r�p�rtoire de t�l�chargement du fichier.
	 */
	private String downloadPath;
	/**
	 * Titre du fichier.
	 */
	private String title;
	/**
	 * Message d'erreur � afficher dans une notification en cas de probl�me.
	 */
	private String errorMsg;
	/**
	 * Permet de v�rifier si le t�l�chargement s'est effectu� avec succ�s ou
	 * non.
	 */
	private boolean done = true;
	/**
	 * Identifiant de la notification.
	 */
	private int idNotification;
	/**
	 * Permet la gestion des notifications.
	 */
	private NotificationManager notificationManager;
	/**
	 * Context de l'Activit� ayant d�marr�e le t�l�chargement.
	 */
	private Context context;
	/**
	 * Taille du buffer.
	 */
	private static final int BUFFER_SIZE = 1024;
	/**
	 * Le nombre maximal de l'�chelle de progression.
	 */
	private static final int MAX_PROGRESS = 100;
	/**
	 * Notifier � chaque avancement d'au moins 5%.
	 */
	private static final int NOTIF_UPDATE = 5;

	/**
	 * @param c
	 *            Context de l'Activit� ayant d�marr�e le t�l�chargement
	 * @param titl
	 *            Titre du fichier
	 * @param ext
	 *            extention du fichier
	 * @param id
	 *            id du fichier
	 * @param mimetype
	 *            type du fichier
	 */
	public DownloadFile(final Context c, final String titl) {
		super();
		context = c;
		title = titl;
		downloadPath = Environment.getExternalStorageDirectory().getPath()
				+ "/" + Settings.DOWNLOAD_PATH + "/";
		startDownloadNotify();
	}

	/**
	 * afficher une notification indiquant le d�but du t�l�chargement.
	 */
	private void startDownloadNotify() {

		idNotification = 100;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification.Builder(context)
				.setContentTitle(context.getString(R.string.start_download))
				.setSmallIcon(android.R.drawable.stat_sys_download).build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(idNotification, notification);
	}

	/**
	 * afficher une notification exposant l'�volution du t�l�chargement.
	 * 
	 * @param pourcen
	 *            avancement du t�l�chargement
	 */
	private void updateDownloadNotif(final int pourcen) {
		CharSequence contentText = "" + pourcen + "%";
		Notification notification = new Notification.Builder(context)
				.setContentTitle(title).setContentText(contentText)
				.setSmallIcon(android.R.drawable.stat_sys_download).build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(idNotification, notification);
	}

	/**
	 * afficher une notification la fin du t�l�chargement et que le fichier est
	 * pr�t � �tre lu.
	 * 
	 * @param path
	 *            chemin du fichier t�l�charg�
	 * @param mimetype
	 *            type du fichier t�l�charg�
	 */
	private void ready(final String path) {
		notificationManager.cancel(idNotification);
		Notification notification = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(context.getString(R.string.download_ends))
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(idNotification, notification);
	}

	/**
	 * afficher une notification l'echec fin du t�l�chargement.
	 * 
	 * @param msg
	 *            message � affich� dans la notification
	 */
	private void failDownloadNotif(final String msg) {
		notificationManager.cancel(idNotification);
		Notification notification = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(msg)
				.setSmallIcon(android.R.drawable.stat_notify_error).build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(idNotification, notification);
	}

	@Override
	protected final String doInBackground(final String... urlFile) {

		// Cr�ation du r�pertoire cible s'il n'existe pas
		File wallpaperDirectory = new File(downloadPath);
		wallpaperDirectory.mkdirs();

		int count;
		try {
			HttpEntity entity = getEntity(urlFile[0]);
			if (entity != null) {
				InputStream input = entity.getContent();
				entity.getContentLength();
				try {
					OutputStream output = new FileOutputStream(downloadPath
							+ title);
					byte[] data = new byte[BUFFER_SIZE];
					long total = 0;
					int pourcent = 0;
					int ancienPourcent = 0;
					final long lenghtOfFile = entity.getContentLength();
					while ((count = input.read(data)) != -1) {
						total += count;
						pourcent = (int) (total * MAX_PROGRESS / lenghtOfFile);
						if ((pourcent >= (ancienPourcent + NOTIF_UPDATE))
								|| pourcent == MAX_PROGRESS) {
							ancienPourcent = pourcent;
							publishProgress(pourcent);
						}
						output.write(data, 0, count);
					}
					output.flush();
					output.close();
					input.close();
				} catch (IOException e) {
					done = false;
					errorMsg = context.getString(R.string.no_memory_space);
				}
			}
		} catch (IOException e) {
			done = false;
			errorMsg = context.getString(R.string.cnx_pb);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected final void onProgressUpdate(final Integer... args) {
		// afficher une notification exposant l'�volution du t�l�chargement
		updateDownloadNotif(args[0]);
	}

	// Fin du t�l�chargement
	@Override
	protected final void onPostExecute(final Object result) {
		super.onPostExecute(result);
		if (done) {
			ready(downloadPath + title);
		} else {
			failDownloadNotif(errorMsg);
		}
	}

	public final HttpEntity getEntity(final String url) throws IOException {
		HttpGet httpget = new HttpGet(URIUtil.encodeQuery(url));
		HttpResponse response;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		response = httpclient.execute(httpget);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException();
		}
		HttpEntity entity = response.getEntity();
		return entity;
	}
}