package com.jps.pics.tagger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Text;

public class Example {
	Image dogImage;
	Text dogNameText;
	Combo dogBreedCombo;
	Canvas dogPhoto;
	List categories;
	Text nameText;
	Text phoneText;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Example().createShell(display);
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
	}

	public Shell createShell(final Display display) {
		final Shell shell = new Shell(display);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		shell.setLayout(layout);
		shell.setText("Dog Show Entry");

		Group ownerInfo = new Group(shell, SWT.NONE);
		ownerInfo.setText("Owner Info");
		FormLayout ownerLayout = new FormLayout();
		ownerLayout.marginWidth = 5;
		ownerLayout.marginHeight = 5;
		ownerInfo.setLayout(ownerLayout);

		Label dogName = new Label(shell, SWT.NONE);
		dogName.setText("Dog's Name:");
		dogNameText = new Text(shell, SWT.SINGLE | SWT.BORDER);

		Label dogBreed = new Label(shell, SWT.NONE);
		dogBreed.setText("Breed:");

		dogBreedCombo = new Combo(shell, SWT.NONE);
		dogBreedCombo.setItems(new String[] { "Collie", "Pitbull", "Poodle", "Scottie", "Black Lab" });

		Label photo = new Label(shell, SWT.NONE);
		photo.setText("Photo:");
		dogPhoto = new Canvas(shell, SWT.BORDER);

		Button browse = new Button(shell, SWT.PUSH);
		browse.setText("Browse...");

		Button delete = new Button(shell, SWT.PUSH);
		delete.setText("Delete");

		Label cats = new Label(shell, SWT.NONE);
		cats.setText("Categories");
		categories = new List(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		categories.setItems(new String[] { "Best of Breed", "Prettiest Female", "Handsomest Male", "Best Dressed", "Fluffiest Ears", "Most Colors",
				"Best Performer", "Loudest Bark", "Best Behaved", "Prettiest Eyes", "Most Hair", "Longest Tail", "Cutest Trick" });

		Button enter = new Button(shell, SWT.PUSH);
		enter.setText("Enter");
		FormData data = new FormData();
		data.top = new FormAttachment(dogNameText, 0, SWT.CENTER);
		dogName.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(dogName, 5);
		data.right = new FormAttachment(100, 0);
		dogNameText.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(dogBreedCombo, 0, SWT.CENTER);
		dogBreed.setLayoutData(data);
		data = new FormData();
		data.top = new FormAttachment(dogNameText, 5);
		data.left = new FormAttachment(dogNameText, 0, SWT.LEFT);
		data.right = new FormAttachment(categories, -5);
		dogBreedCombo.setLayoutData(data);

		data = new FormData(80, 80);
		data.top = new FormAttachment(dogBreedCombo, 5);
		data.left = new FormAttachment(dogNameText, 0, SWT.LEFT);
		data.right = new FormAttachment(categories, -5);
		data.bottom = new FormAttachment(ownerInfo, -5);
		dogPhoto.setLayoutData(data);
		dogPhoto.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if(dogImage != null) {
					event.gc.drawImage(dogImage, 0, 0);
				}
			}
		});
		data = new FormData();
		data.top = new FormAttachment(dogPhoto, 0, SWT.TOP);
		photo.setLayoutData(data);
		data = new FormData();
		data.top = new FormAttachment(photo, 5);
		data.right = new FormAttachment(dogPhoto, -5);
		browse.setLayoutData(data);
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String fileName = new FileDialog(shell).open();
				if(fileName != null) {
					dogImage = new Image(display, fileName);
				}
			}
		});

		data = new FormData();
		data.left = new FormAttachment(browse, 0, SWT.LEFT);
		data.top = new FormAttachment(browse, 5);
		data.right = new FormAttachment(dogPhoto, -5);
		delete.setLayoutData(data);
		delete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(dogImage != null) {
					dogImage.dispose();
					dogImage = null;
					dogPhoto.redraw();
				}
			}
		});

		data = new FormData(90, 140);
		data.top = new FormAttachment(dogPhoto, 0, SWT.TOP);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(enter, -5);
		categories.setLayoutData(data);

		data = new FormData();
		data.bottom = new FormAttachment(categories, -5);
		data.left = new FormAttachment(categories, 0, SWT.CENTER);
		cats.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		enter.setLayoutData(data);
		enter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println("\nDog Name: " + dogNameText.getText());
				System.out.println("Dog Breed: " + dogBreedCombo.getText());
				System.out.println("Owner Name: " + nameText.getText());
				System.out.println("Owner Phone: " + phoneText.getText());
				System.out.println("Categories:");
				String cats[] = categories.getSelection();
				for(int i = 0; i < cats.length; i++) {
					System.out.println("\t" + cats[i]);
				}
			}
		});

		data = new FormData();
		data.bottom = new FormAttachment(enter, -5);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(categories, -5);
		ownerInfo.setLayoutData(data);

		Label name = new Label(ownerInfo, SWT.NULL);
		name.setText("Name:");

		Label phone = new Label(ownerInfo, SWT.PUSH);
		phone.setText("Phone:");

		nameText = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);
		phoneText = new Text(ownerInfo, SWT.SINGLE | SWT.BORDER);

		data = new FormData();
		data.top = new FormAttachment(nameText, 0, SWT.CENTER);
		name.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(phoneText, 0, SWT.CENTER);
		phone.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(phone, 5);
		data.right = new FormAttachment(100, 0);
		nameText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameText, 0, SWT.LEFT);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(55, 0);
		phoneText.setLayoutData(data);

		shell.pack();

		return shell;
	}
}