package com.orion.notepro.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.maps.model.LatLng;
import com.mvc.imagepicker.ImagePicker;
import com.orion.notepro.R;
import com.orion.notepro.controller.DatabaseHelper;
import com.orion.notepro.model.Media;
import com.orion.notepro.model.MediaType;
import com.orion.notepro.model.Note;
import com.orion.notepro.model.Subject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NoteDetailActivity extends AppCompatActivity {

    static final String TAG = "NotePro";
    static final int REQUEST_TAKE_PHOTO = 1;

    @BindView(R.id.edtNoteTitle)
    TextInputEditText edtNoteTitle;

    @BindView(R.id.slider)
    SliderLayout sliderShow;

    private String mCurrentPhotoPath;
    private List<Media> medias = new ArrayList<>();
    private Note noteToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        ButterKnife.bind(this);

        initScreen();
    }

    private void initScreen() {
        ImagePicker.setMinQuality(600, 600);
//        setUpToolbar(); Comentado para ser usado quando for implementar o note view
        if (isToEditNote()) {
            Log.i(TAG, "Edit note: " + noteToEdit.toString());
            edtNoteTitle.setText(noteToEdit.getTitle());
            noteToEdit.getMedias().forEach(new Consumer<Media>() {
                @Override
                public void accept(Media media) {
                    addViewToSlider("Test", media.getAudio().getAbsolutePath());
                    medias.add(media);
                }
            });
        }
    }

    private boolean isToEditNote() {
        noteToEdit = (Note) getIntent().getSerializableExtra("note");
        return noteToEdit != null;
    }

    private void addViewToSlider(String description, String imagePath) {
        TextSliderView textSliderView = new TextSliderView(this);
        textSliderView
                .description(description)
                .image(new File(imagePath));

        sliderShow.addSlider(textSliderView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_detail_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_note_menu:
                saveNote();
                return true;
            default:
                Log.w("NotePro", "Menu item not implemented");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        sliderShow.stopAutoCycle();
        super.onStop();
    }

    @OnClick(R.id.fabPhotos)
    public void dispatchTakePictureIntent() {
        ImagePicker.pickImage(this, "Select your image:", REQUEST_TAKE_PHOTO, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            mCurrentPhotoPath = ImagePicker.getImagePathFromResult(this, requestCode, resultCode, data);
            Log.i(TAG, mCurrentPhotoPath);
            medias.add(new Media(new File(mCurrentPhotoPath), MediaType.PHOTO));
            addViewToSlider("1 photo of 10", mCurrentPhotoPath);
        }
    }

    private void saveNote() {
        Log.i(TAG, edtNoteTitle.getText().toString());
        Note note;
        if (isToEditNote()) {
            noteToEdit.setTitle(edtNoteTitle.getText().toString());
            noteToEdit.setSubject(new Subject(1,"Personal", Color.BLUE));
            noteToEdit.setDateTime(LocalDateTime.now());
            noteToEdit.setLatLng(new LatLng(43.653226, -79.383184));
            note = noteToEdit;
        } else {
            note = new Note(
                edtNoteTitle.getText().toString(),
                "Test Description",
                new Subject(1,"Personal", Color.BLUE),
                LocalDateTime.now(),
                new LatLng(43.653226, -79.383184));
        }

        DatabaseHelper dao = new DatabaseHelper(this);
        note.setMedias(medias);
        dao.save(note);

        //Only to see if the note was saved
        List<Note> notes = dao.selectAllNotes();
        for (Note n : notes) {
            Log.i(TAG, n.toString());
        }
    }

    private void setUpToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("Test");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
