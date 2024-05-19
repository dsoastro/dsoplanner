package com.astro.dsoplanner;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.NgcicObject;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.InfoListStringLoaderObsListImp;

public abstract class Command {
    static final String TAG = Command.class.getSimpleName();

    abstract public void execute();
}

class NewNoteCommand extends Command {

    private static final String BUNDLE = "bundle";

    private Context context;
    private AstroObject obj;
    private Calendar c;
    private String path;

    public NewNoteCommand(Context context, AstroObject obj, Calendar c, String path) {

        this.context = context;
        this.obj = obj;
        this.c = c;
        this.path = path;
    }

    public void execute() {
        Log.d(TAG, "obj=" + obj);
        NoteRequest request = new NoteRequest(obj, c.getTimeInMillis(), path);
        Intent i = new Intent(context, NoteActivity.class);
        i.putExtra(BUNDLE, request.getBundle());
        context.startActivity(i);
    }
}

class GetObjectNotesCommand extends Command {

    private static final String BUNDLE = "bundle";

    private Context context;
    private AstroObject obj;

    public GetObjectNotesCommand(Context context, AstroObject obj) {

        this.context = context;
        this.obj = obj;
    }

    public void execute() {
        NoteRequest request = new NoteRequest(obj, NoteRequest.GET_OBJECT_NOTES);
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(BUNDLE, request.getBundle());
        context.startActivity(intent);
    }
}

class GetAllNotesCommand extends Command {
    private static final String BUNDLE = "bundle";
    private Context context;

    public GetAllNotesCommand(Context context) {
        this.context = context;
    }

    public void execute() {
        NoteRequest request = new NoteRequest(null, NoteRequest.GET_ALL_NOTES);
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(BUNDLE, request.getBundle());
        context.startActivity(intent);
    }

}

class SearchNotes extends Command {
    private static final String BUNDLE = "bundle";
    private Context context;
    private String searchString;

    public SearchNotes(Context context, String searchString) {
        this.context = context;
        this.searchString = searchString;
    }

    public void execute() {
        NoteRequest request = new NoteRequest(searchString);
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(BUNDLE, request.getBundle());
        context.startActivity(intent);
    }
}

class PictureCommand extends Command {
    private static final String ARRAY = "array";
    private static final String NAME = "name";
    private AstroObject obj = null;
    private Context context;
    private NoteRecord nr = null;
    private static final int MESSAGE_IC = 1;
    private static final int MESSAGE_NGC = 2;
    private static final int MESSAGE_CUSTOM_OBJECT = 3;

    public PictureCommand(Context context, AstroObject obj) {
        this.context = context;
        this.obj = obj;
    }

    public PictureCommand(Context context, NoteRecord nr) {
        this.context = context;
        this.nr = nr;
    }

    public void execute() {
        Log.d(TAG, "PictureCommand, execute");
        Intent w = new Intent(context, PictureActivity.class);
        if (obj == null && nr != null) {
            if (nr.catalog == AstroCatalog.NGCIC_CATALOG) {
                obj = new NoteDatabase().getObject(nr, new ErrorHandler());
            }
        }

        if (obj != null) {
            List<String> paths = DetailsActivity.getPicturePaths(context, obj);
            for (String s : paths) {
                Log.d(TAG, "path=" + s);
            }
            if (paths.size() > 0) {
                w.putExtra(NAME, obj.getLongName());
                String[] arr = paths.toArray(new String[paths.size()]);
                w.putExtra(ARRAY, arr);
                context.startActivity(w);
                return;
            } else {
                if (obj instanceof NgcicObject) {
                    if (((NgcicObject) obj).isIc()) showMessage(MESSAGE_IC);
                    else showMessage(MESSAGE_NGC);
                } else {
                    showMessage(MESSAGE_CUSTOM_OBJECT);
                }
                return;
            }
        }

        showMessage(MESSAGE_CUSTOM_OBJECT);
    }

    private void showMessage(int m) {
        String message = "";
        switch (m) {
            case MESSAGE_IC:
                message = context.getString(R.string.there_are_no_images_for_ic_objects);
                break;
            case MESSAGE_NGC:
                message = context.getString(R.string.images_for_ngc_objects_are_missing_);
                break;
            case MESSAGE_CUSTOM_OBJECT:
                message = context.getString(R.string.there_is_no_image_for_this_object);
                break;
        }

        InputDialog.message(context, message, 0).show();
    }
}

class ShareCommand extends Command {
    private static final String TEXT_PLAIN = "text/plain";
    String s;
    Context context;

    public ShareCommand(Context context, String s) {
        this.s = s;
        this.context = context;
    }

    public void execute() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, s);
        sendIntent.setType(TEXT_PLAIN);
        context.startActivity(sendIntent);
    }
}

class PasteCommand extends Command {
    private InfoList list;
    private Runnable r;
    private Activity context;
    private int listNumber;
    String warning;

    /**
     * PasteCommand considers clipboard content as coming from external sources,
     * and thus references to user databases and not considered when inflating objects!
     *
     * @param list       - list to paste into
     * @param r          - Runnable to be executed after paste, e.g. updating the screen etc
     *                   to be performed in UI thread!!!
     * @param listNumber - needed only to determine if we deal with Observation List,
     *                   because it requires a different ListLoader
     */
    public PasteCommand(InfoList list, int listNumber, Runnable r, Activity context, String warning) {
        this.list = list;
        this.r = r;
        this.context = context;
        this.listNumber = listNumber;
        this.warning = warning;
    }

    private InfoListLoader paste(int infoListNumber) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String content = "";

        if (clipboard.hasText()) {
            content = clipboard.getText().toString();
        }
        if (infoListNumber >= InfoList.PrimaryObsList && infoListNumber <= InfoList.PrimaryObsList + 3) {
            InfoListStringLoaderObsListImp loader = new InfoListStringLoaderObsListImp(content);
            loader.setIgnoreCustomDbRefsFlag();
            return loader;
        } else {
            InfoListStringLoaderImp loader = new InfoListStringLoaderImp(content);
            loader.setIgnoreCustomDbRefsFlag();
            return loader;
        }
    }

    public void execute() {
        final String name = list.getListName();
        Runnable r1 = new Runnable() {
            public void run() {
                try {
                    InfoListLoader loader = paste(listNumber);
                    ErrorHandler eh = list.load(loader);
                    if (eh.hasError()) eh.showError(context);
                } catch (Exception e) {
                }
                list.setListName(name);//do not change name of the list after loading
                r.run();
            }
        };
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (!clipboard.hasText()) {
            InputDialog.message(context, R.string.no_data_to_paste_).show();
            return;
        }
        Log.d(TAG, "" + clipboard.getText());
        if ("".equals(warning) || (warning == null)) {
            r1.run();
        } else {
            InputDialog d = AstroTools.getDialog(context, warning, r1);
            d.show();
        }
    }
}

