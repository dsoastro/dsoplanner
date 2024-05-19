package com.astro.dsoplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.astro.dsoplanner.base.AstroObject;

public class DetailsCommand extends Command {
    AstroObject obj;
    Context context;

    public DetailsCommand(AstroObject obj, Context context) {
        this.obj = obj;
        this.context = context;
    }

    boolean nomore = false;

    /**
     * @param obj
     * @param context
     * @param nomore  - if true, more btn in details is hidden
     */
    public DetailsCommand(AstroObject obj, Context context, boolean nomore) {
        this(obj, context);
        this.nomore = nomore;
    }

    boolean callerGraph = false;

    /**
     * used for indicating that Graph is the calling activity
     */
    public void setCallerGraph() {
        callerGraph = true;
    }

    boolean returngraph = false;
    int code = 0;

    /**
     * used for indicating that if sky view is pressed in Details it should leave with setting result
     */
    public void setReturnGraphFlag(int code) {
        returngraph = true;
        this.code = code;
    }

    boolean addoff = false;

    /**
     * used for turning add btn off
     */
    public void setAddBtnOff() {
        addoff = true;
    }

    public void execute() {
        Bundle b = AstroTools.getBundleFromExportable(obj);
        Intent i = new Intent(context, DetailsActivity.class);
        i.putExtra(Constants.ASTRO_OBJECT_INTENT, b);
        i.putExtra(Constants.NO_MORE_BUTTON, nomore);
        i.putExtra(Constants.CALLER_GRAPH, callerGraph);
        i.putExtra(Constants.RETURN_TO_GRAPH, returngraph);
        i.putExtra(Constants.ADD_BTN_OFF, addoff);
        if (returngraph) {
            Activity a = (Activity) context;//context is surely an activity in this case
            a.startActivityForResult(i, code);
        } else context.startActivity(i);
    }
}
