package com.astro.dsoplanner.graph;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.scopedrivers.CommunicationManager;

import java.util.Calendar;

public class DscDialog {
    private static final String TAG = "DscDialog";
    GraphActivity graphActivity;
    InputDialog currentDialog;
    Point p;
    DscRec.Stage stage;

    public DscDialog(GraphActivity graphActivity) {
        this.graphActivity = graphActivity;

    }
    public Dialog create(){
        stage = Global.dscR.getStage();
        Log.d(TAG, "stage=" + stage);
        InputDialog dlg = new InputDialog(graphActivity);
        dlg.setTitle("Digital Setting Circles");
        dlg.setPositiveButton("Done");
        dlg.insertLayout(R.layout.dsc_dialog);
        Button btn_set = (Button) dlg.findViewById(R.id.dsc_set);

        if (stage == DscRec.Stage.None)  {
            btn_set.setText("Set first star");

            btn_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommunicationManager.getComModule().read();
                }
            });

        }
        else if (stage == DscRec.Stage.First){
            btn_set.setText("Set second star");

            btn_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommunicationManager.getComModule().read();
                }
            });
        }
        else {
            btn_set.setText("Both stars set already");
            btn_set.setEnabled(false);
        }
        Button btn_reset = (Button) dlg.findViewById(R.id.dsc_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Global.dscR.reset();
                stage = Global.dscR.getStage();
                btn_set.setText("Set first star");
                btn_set.setEnabled(true);
                btn_set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CommunicationManager.getComModule().read();
                    }
                });

            }
        });


        p = graphActivity.getSkyView().getObjCursor().getObjSelected();
        TextView objInfo = (TextView)dlg.findViewById(R.id.dsc_obj_info);
        if (p != null && p instanceof AstroObject) {
            String name = ((AstroObject) p).getLongName();
            objInfo.setText(name);
        }


        //dlg.setNegativeButton(graph1243.getString(R.string.cancel));
        currentDialog = dlg;
        return dlg;

    }
    public boolean isDialogShowing(){
        if (currentDialog != null)
            return currentDialog.isShowing();
        else
            return false;
    }
    private void updateBtnSet(String name){
        Button btn_set = (Button) currentDialog.findViewById(R.id.dsc_set);
        btn_set.setText(name);
    }
    public void onDataReceived(double az, double alt){
        long time = Calendar.getInstance().getTimeInMillis();
        if (stage == DscRec.Stage.None){
            Global.dscR.star1 = p;
            Global.dscR.star1_az = az;
            Global.dscR.star1_alt = alt;
            Global.dscR.time1 = time;
            updateBtnSet("First star set");
            Global.dscR.setStage(DscRec.Stage.First);
        }
        else if (stage == DscRec.Stage.First){
            Global.dscR.star2 = p;
            Global.dscR.star2_az = az;
            Global.dscR.star2_alt = alt;
            Global.dscR.time2 = time;
            Global.dscR.setStage(DscRec.Stage.Second);
            updateBtnSet("Second star set");
            if (Global.dscR.isReady()) {
                Global.dscR.makeFix(graphActivity);
                InputDialog.toast("Fix made", graphActivity).show();
            }
        }

    }
    public AstroTools.RaDecRec calculateRaDec(XY star_scope, long timeInMillis){
        return Global.dscR.calculateRaDec(star_scope, timeInMillis);
    }

}
