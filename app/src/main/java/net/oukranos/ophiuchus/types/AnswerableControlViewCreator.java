package net.oukranos.ophiuchus.types;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.oukranos.ophiuchus.R;
import net.oukranos.ophiuchus.interfaces.DataChangeListener;
import net.oukranos.ophiuchus.utils.Logger;

/**
 * Created by francis on 3/24/16.
 */
public class AnswerableControlViewCreator {
    private enum ControlType {
        UNKNOWN, TEXT_FIELD, DROP_DOWN, CHECK_BOX
    }

    private AnswerableControlViewCreator() {
        return;
    }

    public static View createView(Context context, AnswerableControlData data, View convertView,
                                  ViewGroup parent, DataChangeListener dataListener) {
        ControlType ctrlCode = getControlTypeCode(data.getControlType());

        /* Obtain the correct XML layout for this control type */
        int iCtrlResourceId = getControlTemplateResource(ctrlCode);
        if (iCtrlResourceId <= 0) {
            return convertView;
        }

        /* Inflate this into our convertView */
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(iCtrlResourceId, parent, false);

        /* Initialize the view (may differ for each control type */
        initializeView(ctrlCode, convertView, data, dataListener);

        return convertView;
    }

    /* PRIVATE METHODS */
    private static void initializeView(ControlType code, View convertView,
                                       AnswerableControlData data, DataChangeListener dataListener) {
        switch (code) {
            case TEXT_FIELD:
                initializeAsTextFieldControl(convertView, data, dataListener);
                break;
            case DROP_DOWN:
                initializeAsDropDownControl(convertView, data, dataListener);
                break;
            case CHECK_BOX:
                initializeAsCheckBoxControl(convertView, data, dataListener);
                break;
            default:
                break;
        }

        return;
    }

    private static void initializeAsTextFieldControl(View convertView,
                                                     final AnswerableControlData data,
                                                     final DataChangeListener dataListener) {
        TextView txvLabel = (TextView) convertView.findViewById(R.id.txv_label);
        txvLabel.setText(data.getLabel());

        TextView txvSub = (TextView) convertView.findViewById(R.id.txv_misc);
        txvSub.setText("(" + data.getSuffix() + ")");

        final Context context = convertView.getContext();
        final EditText edtValue = (EditText) convertView.findViewById(R.id.edt_value);
        edtValue.setText(data.getValue());
        edtValue.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            startEditTextDialog(context, edtValue, data, dataListener);
                            return true;
                        }
                        return false;
                    }
                }
        );
        return;
    }

    private static void startEditTextDialog(Context context,
                                            final EditText originalTextInput,
                                            final AnswerableControlData data,
                                            final DataChangeListener dataListener) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
        final EditText edtTextInput = new EditText(context);
        edtTextInput.setText(data.getValue());

        dlgBuilder.setTitle("Modify " + data.getLabel())
                .setMessage("Input the new value for " + data.getLabel() + " here")
                .setView(edtTextInput)
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = edtTextInput.getText().toString();
                        if (!value.matches(data.getRestrictions())) {
                            Logger.warn("Warning: Regex not matched by " + value);
                            return;
                        }

                        if (value.equals(data.getValue())) {
                            return;
                        }

                        data.setValue(value);
                        dataListener.onDataChanged(data.getName());
                        return;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        return;
                    }
                });

        dlgBuilder.create().show();

        return;
    }

    private static void initializeAsDropDownControl(View convertView,
                                                    final AnswerableControlData data,
                                                    final DataChangeListener dataListener) {
        TextView txvLabel = (TextView) convertView.findViewById(R.id.txv_label);
        txvLabel.setText(data.getLabel());

        TextView txvSub = (TextView) convertView.findViewById(R.id.txv_misc);
        txvSub.setText("(" + data.getSuffix() + ")");

        final Spinner spinner = (Spinner) convertView.findViewById(R.id.spn_value);

        String spinnerValues =
                generateCsvOptions(data.getDataType(), data.getRestrictions(), data.getSuffix());
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(convertView.getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        spinnerValues.split(","));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String value;
                        try {
                            value = adapter.getItem(position);
                            if (!data.getDataType().equals("string")) {
                                value = value.replace(" " + data.getSuffix(), "");
                            }
                        } catch (Exception e) {
                            Logger.err("Exception occurred: " + e.getMessage());
                            value = "";
                        }

                        if (value.equals(data.getValue())) {
                            return;
                        }

                        data.setValue(value);
                        dataListener.onDataChanged(data.getName());
                        return;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        return;
                    }
                }
        );

        spinner.setFocusable(false);
        spinner.setFocusableInTouchMode(false);

        // TODO
        return;
    }

    private static void initializeAsCheckBoxControl(View convertView,
                                                    final AnswerableControlData data,
                                                    final DataChangeListener dataListener) {
        CheckBox chControl = (CheckBox) convertView.findViewById(R.id.chb_label);
        chControl.setText(data.getLabel());

        chControl.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String value = isChecked ? "Yes" : "No";
                        if (value.equals(data.getValue())) {
                            return;
                        }
                        data.setValue(value);
                        dataListener.onDataChanged(data.getName());
                        return;
                    }
                }
        );
        return;
    }

    private static String generateCsvOptions(String type, String extra, String suffix) {
        /* Initialize our output String */
        String csvStr = "";

        if (type.equals("string")) {
            return extra;
        } else if (type.equals("float")) {
            String opt[] = extra.split(",");

            /* The first part of the string contains our range, separated by a '-' */
            String rangeStr[] = opt[0].split("-");

            float dCountRange = Float.parseFloat(rangeStr[0]);
            float dMaxRange = Float.parseFloat(rangeStr[1]);

            /* The second part of the string contains our increment */
            float dIncrement = Float.parseFloat(opt[1]);

            /* Initialize our output String */
            csvStr = "";

            /* Use absolute values in case dIncrement is negative */
            boolean isFirstLoop = true;
            while (Math.abs(dCountRange) < Math.abs(dMaxRange)) {
                if (isFirstLoop) {
                    isFirstLoop = false;
                } else {
                    csvStr += ",";
                }

                csvStr += Float.toString(dCountRange);
                csvStr += " " + suffix;
                dCountRange += dIncrement;
            }
        } else if (type.equals("int")) {
            String opt[] = extra.split(",");

            /* The first part of the string contains our range, separated by a '-' */
            String rangeStr[] = opt[0].split("-");

            int iCountRange = Integer.parseInt(rangeStr[0]);
            int iMaxRange = Integer.parseInt(rangeStr[1]);

            /* The second part of the string contains our increment */
            float iIncrement = Integer.parseInt(opt[1]);

            /* Use absolute values in case dIncrement is negative */
            boolean isFirstLoop = true;
            while (Math.abs(iCountRange) < Math.abs(iMaxRange)) {
                if (isFirstLoop) {
                    isFirstLoop = false;
                } else {
                    csvStr += ",";
                }

                csvStr += Integer.toString(iCountRange);
                csvStr += " " + suffix;
                iCountRange += iIncrement;
            }
        }

        return csvStr;
    }

    private static int getControlTemplateResource(ControlType type) {
        switch (type) {
            case TEXT_FIELD:
                return R.layout.list_control_item_txt_field;
            case DROP_DOWN:
                return R.layout.list_control_item_drop_down;
            case CHECK_BOX:
                return R.layout.list_control_item_check_box;
            default:
                break;
        }

        return -1;
    }

    private static ControlType getControlTypeCode(String type) {
        if (type.equals("text_field")) {
            return ControlType.TEXT_FIELD;
        } else if (type.equals("drop_down")) {
            return ControlType.DROP_DOWN;
        } else if (type.equals("check_box")) {
            return ControlType.CHECK_BOX;
        }

        return ControlType.UNKNOWN;
    }

}
