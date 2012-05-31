package com.jps.pics.tagger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;

public class Application {

	private Display display;
	private Shell shell;
	private Canvas picPreview;
	private Image picImage;

	private final Logger logger = Logger.getLogger(Application.class);

	public Application() {
		display = new Display();
		shell = new Shell(display);
		shell.setText("Pic's Tagger");

		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 2;
		formLayout.marginWidth = 2;
		shell.setLayout(formLayout);

		shell.setSize(1024, 768);
		shell.setMaximized(false);

		this.initUI();

		shell.pack();
		shell.open();
	}

	public void display() {
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * 
	 */
	public void initUI() {
		// -- Creating objects --//
		// exif group
		Group exifGroup = new Group(shell, SWT.NONE);
		exifGroup.setText("EXIF");
		FormLayout exifLayout = new FormLayout();
		exifLayout.marginWidth = 5;
		exifLayout.marginHeight = 5;
		exifGroup.setLayout(exifLayout);

		// exif label
		final Label exifLabel = new Label(exifGroup, SWT.PUSH);
		exifLabel.setText("blah");

		// browse group
		Group filesGroup = new Group(shell, SWT.NONE);
		filesGroup.setText("Images");
		FormLayout filesLayout = new FormLayout();
		filesLayout.marginWidth = 5;
		filesLayout.marginHeight = 5;
		filesGroup.setLayout(filesLayout);

		// file list
		final List fileList = new List(filesGroup, SWT.BORDER | SWT.MULTI);
		fileList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String fileName = fileList.getItem(fileList.getFocusIndex());
				logger.debug("file : " + fileName);

				// exif datas
				//exifLabel.setText(readExifDatas(fileName));

				// pic preview
				picImage = new Image(display, fileName);
				picImage = resize(picImage, picPreview.getClientArea().width);
				picPreview.redraw();
			}
		});

		// add button
		Button addButton = new Button(filesGroup, SWT.PUSH);
		addButton.setText("Ajouter");
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					FileDialog dialog = new FileDialog(shell, SWT.MULTI);
					dialog.setText("Ajouter des images...");
					dialog.setFilterNames(new String[] { "Images jpeg" });
					dialog.setFilterExtensions(new String[] { "*.jp*g" });
					String path = dialog.open();
					if(path != null) {
						File file = new File(path);
						logger.debug("Path : " + file.getParentFile().getCanonicalPath());
						if(file.isFile())
							displayFiles(file.getParentFile().getCanonicalPath(), dialog.getFileNames(), fileList);
						else
							displayFiles(file.getParentFile().getCanonicalPath(), file.list(), fileList);
					}
				}
				catch(IOException e1) {
					logger.error("Probleme de fichiers", e1);
				}
			}
		});

		// delete button
		Button delButton = new Button(filesGroup, SWT.PUSH);
		delButton.setText("Supprimer");
		delButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(fileList.getFocusIndex() > 0) {
					fileList.remove(fileList.getFocusIndex());
				}
			}
		});

		// tag group
		Group tagGroup = new Group(shell, SWT.NONE);
		tagGroup.setText("Tag");
		FormLayout tagLayout = new FormLayout();
		tagLayout.marginWidth = 5;
		tagLayout.marginHeight = 5;
		tagGroup.setLayout(tagLayout);

		// place label and text
		Label placeLabel = new Label(tagGroup, SWT.PUSH);
		placeLabel.setText("Lieu : ");
		final Text placeText = new Text(tagGroup, SWT.BORDER);
		placeText.setText("");
		placeText.setTextLimit(40);

		// tag button
		Button tagButton = new Button(tagGroup, SWT.PUSH);
		tagButton.setText("Tagger les fichiers");
		tagButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileList.setItems(renameFiles(fileList.getItems(), placeText.getText()));
			}
		});

		// image group
		Group imageGroup = new Group(shell, SWT.NONE);
		imageGroup.setText("Image");
		FormLayout imageLayout = new FormLayout();
		imageLayout.marginWidth = 5;
		imageLayout.marginHeight = 5;
		imageGroup.setLayout(imageLayout);

		// pic preview
		picPreview = new Canvas(imageGroup, SWT.BORDER);

		// -- Layout --//
		// browse group
		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(tagGroup, -5, SWT.LEFT);
		formData.bottom = new FormAttachment(100, 0);
		filesGroup.setLayoutData(formData);

		// add button
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(0, 0);
		addButton.setLayoutData(formData);

		// delete button
		formData = new FormData();
		formData.left = new FormAttachment(addButton, 5, SWT.RIGHT);
		formData.top = new FormAttachment(0, 0);
		delButton.setLayoutData(formData);

		// file list
		formData = new FormData();
		formData.width = 400;
		formData.height = 600;
		formData.top = new FormAttachment(addButton, 5, SWT.BOTTOM);
		formData.bottom = new FormAttachment(100, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		fileList.setLayoutData(formData);

		// tag group
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(filesGroup, 0, SWT.RIGHT);
		formData.right = new FormAttachment(100, 0);
		formData.width = 300;
		formData.height = 150;
		tagGroup.setLayoutData(formData);

		// place label and text
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(0, 0);
		placeLabel.setLayoutData(formData);
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(placeLabel, 0, SWT.BOTTOM);
		formData.width = 200;
		placeText.setLayoutData(formData);

		// tag button
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(placeText, 5, SWT.BOTTOM);
		tagButton.setLayoutData(formData);

		// exif group
		formData = new FormData();
		formData.top = new FormAttachment(tagGroup, 0, SWT.BOTTOM);
		formData.left = new FormAttachment(filesGroup, 5, SWT.RIGHT);
		formData.right = new FormAttachment(100, 0);
		exifGroup.setLayoutData(formData);

		// exif label
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		exifLabel.setLayoutData(formData);

		// image group
		formData = new FormData();
		formData.top = new FormAttachment(exifGroup, 0, SWT.BOTTOM);
		formData.left = new FormAttachment(filesGroup, 5, SWT.RIGHT);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		formData.width = 300;
		formData.height = 200;
		imageGroup.setLayoutData(formData);

		// pic preview
		formData = new FormData(80, 80);
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		picPreview.setLayoutData(formData);
		picPreview.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if(picImage != null) {
					event.gc.drawImage(picImage, 0, 0);
				}
			}
		});
	}

	/**
	 * @param path
	 * @param files
	 * @param fileList
	 */
	private void displayFiles(String path, String[] files, List fileList) {
		for(int i = 0; i < files.length; i++) {
			fileList.add(path + "\\" + files[i]);
		}
	}

	/**
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	private Image resize(Image image, int width) {
		int height = (int) (width / ((float) image.getImageData().width / (float) image.getImageData().height));
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose();
		return scaled;
	}

	/**
	 * @param filename
	 * @return
	 */
	private HashMap<String, String> extractExifDatas(String filename) {
		HashMap<String, String> datas = new HashMap<String, String>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm");
		try {
			File jpegFile = new File(filename);
			JpegSegmentReader segmentReader = new JpegSegmentReader(jpegFile);
			byte[] exifSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APP1);
			Metadata metadata = new Metadata();
			new ExifReader(exifSegment).extract(metadata);

			// reading exif
			Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
			Date date = exifDirectory.getDate(ExifDirectory.TAG_DATETIME);
			datas.put("date", formatter.format(date));
		}
		catch(JpegProcessingException e) {
			logger.error("Problem during datas extraction", e);
		}
		catch(MetadataException e) {
			logger.error("Problem during datas extraction", e);
		}
		return datas;
	}

	/**
	 * @param filenames
	 * @param place
	 */
	private String[] renameFiles(String[] filenames, String place) {
		String[] newFilenames = new String[filenames.length];
		logger.debug("Lieu : " + place);
		for(int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			HashMap<String, String> exifDatas = extractExifDatas(filename);
			File pic = new File(filename);
			File newPic;
			try {
				String newFileName = pic.getParentFile().getCanonicalPath() + "\\" + exifDatas.get("date");
				if((place != null) && !"".equals(place)) {
					newFileName += " - " + place;
				}

				// same filename ?
				int index = 0;
				File tempFile = new File(newFileName + " - (" + index + ").jpg");
				while(tempFile.exists() && !tempFile.getName().equals(pic.getName())) {
					index++;
					tempFile = new File(newFileName + " - (" + index + ").jpg");
				}

				newFileName += " - (" + index + ").jpg";
				newPic = new File(newFileName);
				if(pic.renameTo(newPic)) {
					logger.debug(filename + " renommé en " + newPic.getName());
					newFilenames[i] = newPic.getAbsolutePath();
				}
				else {
					logger.debug(filename + " n'a pas été renommé en " + newPic.getName());
					newFilenames[i] = filename;
				}
			}
			catch(IOException e) {
				logger.error("Failed to rename a file", e);
				logger.debug(filename + " n'a pas été renommé");
				newFilenames[i] = filename;
			}
		}
		return newFilenames;
	}
}
