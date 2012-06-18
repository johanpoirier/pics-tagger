package com.jps.pics.tagger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Application {

	private final static String REGISTRY_KEY = "Software\\JoPs";

	private Display display;
	private Shell shell;
	private Canvas picPreview;
	private Image picImage;
	private String[] places;

	public Application() {
		display = new Display();
		shell = new Shell(display);
		shell.setText("Pic's Tagger");

		// registry
		loadPlacesFromRegistry();

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
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
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
		exifLabel.setText("");

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
				System.out.println("file : " + fileName);

				// exif datas
				StringBuffer datas = new StringBuffer();
				Map<String, String> exifDatas = extractExifDatas(fileName);
				for (Iterator<String> i = exifDatas.keySet().iterator(); i.hasNext();) {
					String key = i.next();
					datas.append(key);
					datas.append(" : ");
					datas.append(exifDatas.get(key));
				}
				exifLabel.setText(datas.toString());

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
					if (path != null) {
						File file = new File(path);
						System.out.println("Path : " + file.getParentFile().getCanonicalPath());
						if (file.isFile())
							displayFiles(file.getParentFile().getCanonicalPath(), dialog.getFileNames(), fileList);
						else
							displayFiles(file.getParentFile().getCanonicalPath(), file.list(), fileList);
					}
				} catch (IOException e1) {
					System.err.println("Probleme de fichiers : " + e1.getMessage());
				}
			}
		});

		// delete button
		Button delButton = new Button(filesGroup, SWT.PUSH);
		delButton.setText("Supprimer");
		delButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int[] selection = fileList.getSelectionIndices();
				int offset = 0;
				for (int i = 0; i < selection.length; i++) {
					fileList.remove(i - offset);
					offset++;
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
		final Combo placesList = new Combo(tagGroup, SWT.BORDER);
		placesList.setItems(places);

		// tag button
		Button tagButton = new Button(tagGroup, SWT.PUSH);
		tagButton.setText("Tagger les fichiers");
		tagButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileList.setItems(renameFiles(fileList.getItems(), placesList.getText()));
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
		formData.width = 500;
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
		placesList.setLayoutData(formData);

		// tag button
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(placesList, 5, SWT.BOTTOM);
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
				if (picImage != null) {
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
		for (int i = 0; i < files.length; i++) {
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm.ss");
		try {
			File jpegFile = new File(filename);
			Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

			// reading exif
			ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
			if(directory != null) {
				Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				datas.put("date", formatter.format(date));
			}
		} catch (JpegProcessingException e) {
			System.err.println("Problem during datas extraction : " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Problem during datas extraction" + e.getMessage());
		} catch (ImageProcessingException e) {
			System.err.println("Problem during datas extraction" + e.getMessage());
		}
		return datas;
	}

	/**
	 * @param filenames
	 * @param place
	 */
	private String[] renameFiles(String[] filenames, String place) {
		String[] newFilenames = new String[filenames.length];
		System.out.println("Lieu : " + place);
		addPlaceToRegistry(place);
		for (int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			HashMap<String, String> exifDatas = extractExifDatas(filename);
			File pic = new File(filename);
			File newPic;
			try {
				StringBuffer newFileName = new StringBuffer(pic.getParentFile().getCanonicalPath());
				newFileName.append("\\");
				String date = exifDatas.get("date");
				if(date != null) {
					newFileName.append(exifDatas.get("date"));
					if ((place != null) && !"".equals(place)) {
						newFileName.append(" - " + place);
					}

					// same filename ?
					int index = 0;
					File tempFile = new File(newFileName + " (" + index + ").jpg");
					while (tempFile.exists() && !tempFile.getName().equals(pic.getName())) {
						index++;
						tempFile = new File(newFileName + " (" + index + ").jpg");
					}
	
					if (index > 0) {
						newFileName.append(" (" + index + ")");
					}
					newFileName.append(".jpg");
	
					newPic = new File(newFileName.toString());
					if (pic.renameTo(newPic)) {
						System.out.println(filename + " renommé en " + newPic.getName());
						newFilenames[i] = newPic.getAbsolutePath();
					} else {
						System.out.println(filename + " n'a pas été renommé en " + newPic.getName());
						newFilenames[i] = filename;
					}
				}
				else {
					System.out.println(filename + " n'a pas été renommé car pas d'EXIF.");
					newFilenames[i] = filename;
				}
			} catch (IOException e) {
				System.err.println("Failed to rename a file : " + e.getMessage());
				System.out.println(filename + " n'a pas été renommé");
				newFilenames[i] = filename;
			}
		}
		return newFilenames;
	}

	private void loadPlacesFromRegistry() {
		try {
			String placesListString = Registry.readString(Registry.HKEY_CURRENT_USER, REGISTRY_KEY, "places");
			if (placesListString == null) {
				Registry.createKey(Registry.HKEY_CURRENT_USER, REGISTRY_KEY);
			} else {
				placesListString = Registry.readString(Registry.HKEY_CURRENT_USER, REGISTRY_KEY, "places");
				if (placesListString != null) {
					String[] placesFromRegistry = placesListString.split(",");
					places = new String[placesFromRegistry.length];
					int nbPlaces = places.length;
					for (int i = places.length; i-- > 0;) {
						if (placesFromRegistry[i].length() > 0) {
							places[nbPlaces - i - 1] = placesFromRegistry[i].trim();
							System.out.println("Place : " + placesFromRegistry[i]);
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		if (places == null) {
			places = new String[0];
		}
	}

	private void addPlaceToRegistry(String place) {
		StringBuffer newPlaces = new StringBuffer();
		for (int i = 0; i < places.length; i++) {
			if (!place.equalsIgnoreCase(places[i])) {
				newPlaces.append(places[i]);
				newPlaces.append(",");
			}
		}
		newPlaces.append(place);
		try {
			Registry.writeStringValue(Registry.HKEY_CURRENT_USER, REGISTRY_KEY, "places", newPlaces.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
