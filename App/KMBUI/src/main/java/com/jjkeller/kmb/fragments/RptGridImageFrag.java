package com.jjkeller.kmb.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.series.XYSeries;
import com.androidplot.util.PaintUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmb.share.GridLogData;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RptGridImageFrag extends BaseFragment {
    private View _gridImageWrapper;
    private XYPlot _mySimpleXYPlot;
    private TextView _lblOffDutyWellsite;
    private TextView _lblOffDutyHours;
    private TextView _lblSleeperHours;
    private TextView _lblDrivingHours;
    private TextView _lblOnDutyHours;
    private TextView _lblOffDutyWellsiteHours;
    private TextView _tvOdo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.f_rptgridimage, container, false);
        findControls(v);
        return v;
    }

    @SuppressLint("InlinedApi")
    protected void findControls(View v) {
        _gridImageWrapper = v.findViewById(R.id.layoutGridImage);

        _mySimpleXYPlot = (XYPlot) v.findViewById(R.id.mySimpleXYPlot);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            _mySimpleXYPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        _lblOffDutyWellsite = (TextView) v.findViewById(R.id.lblOffDutyWellsite);
        _lblOffDutyHours = (TextView) v.findViewById(R.id.lblOffDutyHours);
        _lblSleeperHours = (TextView) v.findViewById(R.id.lblSleeperHours);
        _lblDrivingHours = (TextView) v.findViewById(R.id.lblDrivingHours);
        _lblOnDutyHours = (TextView) v.findViewById(R.id.lblOnDutyHours);
        _lblOffDutyWellsiteHours = (TextView) v.findViewById(R.id.lblOffDutyWellsiteHours);
        _tvOdo = (TextView) v.findViewById(R.id.tvOdo);
    }

    public View getGridImageWrapper() {
        return _gridImageWrapper;
    }

    public XYPlot getImagePlot() {
        return _mySimpleXYPlot;
    }

    public TextView getOffDutyWellsiteLabel() {
        return _lblOffDutyWellsite;
    }

    public TextView getOffDutyHoursLabel() {
        return _lblOffDutyHours;
    }

    public TextView getSleeperHoursLabel() {
        return _lblSleeperHours;
    }

    public TextView getDrivingHoursLabel() {
        return _lblDrivingHours;
    }

    public TextView getOnDutyHoursLabel() {
        return _lblOnDutyHours;
    }

    public TextView getOffDutyWellsiteHoursLabel() {
        return _lblOffDutyWellsiteHours;
    }

    public void setGridHours(int offDutyHours, int sleeperHours, int onDutyHours, int drivingHours, int offDutyWellSiteHours, ExemptLogTypeEnum exemptLogTypeEnum) {

        if (exemptLogTypeEnum.getValue() == ExemptLogTypeEnum.NULL) {
            getSleeperHoursLabel().setVisibility(View.VISIBLE);
            getDrivingHoursLabel().setVisibility(View.VISIBLE);

            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                getOffDutyHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(offDutyHours + offDutyWellSiteHours));
                getSleeperHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(sleeperHours));
                getOnDutyHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(onDutyHours));
                getDrivingHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(drivingHours));
            } else {
                getOffDutyHoursLabel().setText(this.formatGridHoursLabelText(offDutyHours));
                getSleeperHoursLabel().setText(this.formatGridHoursLabelText(sleeperHours));
                getOnDutyHoursLabel().setText(this.formatGridHoursLabelText(onDutyHours));
                getDrivingHoursLabel().setText(this.formatGridHoursLabelText(drivingHours));
                getOffDutyWellsiteHoursLabel().setText(this.formatGridHoursLabelText(offDutyWellSiteHours));
            }
        } else {
            getSleeperHoursLabel().setVisibility(View.INVISIBLE);
            getDrivingHoursLabel().setVisibility(View.INVISIBLE);

            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                getOffDutyHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(offDutyHours + sleeperHours + offDutyWellSiteHours));
                getOnDutyHoursLabel().setText(DateUtility.getHHmmssFromMilliseconds(onDutyHours + drivingHours));
            } else {
                getOffDutyHoursLabel().setText(this.formatGridHoursLabelText(offDutyHours + sleeperHours));
                getOnDutyHoursLabel().setText(this.formatGridHoursLabelText(onDutyHours + drivingHours));
                getOffDutyWellsiteHoursLabel().setText(this.formatGridHoursLabelText(offDutyWellSiteHours));
            }
        }

        IAPIController controller = MandateObjectFactory.getInstance(getActivity(), GlobalState.getInstance().getFeatureService()).getCurrentEventController();
        EmployeeLog empLog = controller.getSelectedLogForReport();
        if (empLog.getRuleset().isCanadianRuleset()) {
            String odomData = controller.getOdometerData(empLog);
            _tvOdo.setText(odomData);
        }

    }

    private String formatGridHoursLabelText(int minutesTotal) {
        int hours = minutesTotal / 60;
        int minutes = minutesTotal % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    public void FormatGrid(GridLogData data) {
        XYPlot mySimpleXYPlot = getImagePlot();

        float pixVal1dip = PaintUtils.dpToPix(getActivity(), 1);
        boolean isHighDensity = pixVal1dip > 2F;

        mySimpleXYPlot.setTitle(String.format("Log for %s", data.getLogDate()));
        mySimpleXYPlot.setPadding(4, 4, 4, 4);
        mySimpleXYPlot.getTitleWidget().setPadding(4F, 4F, 4F, 4F);
        mySimpleXYPlot.getTitleWidget().setWidth(pixVal1dip * 300);
        if (isHighDensity)
            mySimpleXYPlot.getTitleWidget().setHeight(pixVal1dip * 20);
        else
            mySimpleXYPlot.getTitleWidget().setHeight(pixVal1dip * 10);

        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        mySimpleXYPlot.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);

        mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
        mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);


        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        mySimpleXYPlot.getBorderPaint().setStrokeWidth(1);
        mySimpleXYPlot.getBorderPaint().setAntiAlias(false);
        mySimpleXYPlot.getBorderPaint().setColor(Color.WHITE);

        mySimpleXYPlot.getGraphWidget().setPaddingLeft(pixVal1dip * 10);

        mySimpleXYPlot.getGraphWidget().setPaddingRight(pixVal1dip * 30);

        if (isHighDensity) {
            mySimpleXYPlot.getGraphWidget().setPaddingLeft(pixVal1dip * 10);
            mySimpleXYPlot.getGraphWidget().setPaddingTop(pixVal1dip * 10);
            mySimpleXYPlot.getGraphWidget().setPaddingBottom(pixVal1dip * 10);

            // make room for Mandate hh:mm:ss
            if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
                mySimpleXYPlot.getGraphWidget().setPaddingRight(pixVal1dip * 45);
            }
        } else {
            // on lower density screen, adjust this down a little so that look closer to the same size
            mySimpleXYPlot.getGraphWidget().setPaddingLeft(pixVal1dip);
            mySimpleXYPlot.getGraphWidget().setPaddingTop(pixVal1dip);

            // make room for Mandate hh:mm:ss
            mySimpleXYPlot.getGraphWidget().setPaddingRight(pixVal1dip * (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() ? 30 : 25));

            mySimpleXYPlot.getGraphWidget().setPaddingBottom(pixVal1dip);
        }

        // draw a domain tick for each 3 hour interval (12a, 3a, 6a, 9a, 12p, 3p, 6p, 9p, 12a)
        mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 3600);

        if (isHighDensity) {
            mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setTextSize(pixVal1dip * 10);
            mySimpleXYPlot.getGraphWidget().getDomainOriginLabelPaint().setTextSize(pixVal1dip * 10);
            mySimpleXYPlot.getTitleWidget().getLabelPaint().setTextSize(pixVal1dip * 10);
        } else {
            mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setTextSize(pixVal1dip * 8);
            mySimpleXYPlot.getGraphWidget().getDomainOriginLabelPaint().setTextSize(pixVal1dip * 8);
            mySimpleXYPlot.getTitleWidget().getLabelPaint().setTextSize(pixVal1dip * 8);
        }

        // draw a range tick every duty status
        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);

        if (data.getIsOperateSpecificVehicleForOilField() && !GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
            mySimpleXYPlot.setRangeBoundaries(0, 5, BoundaryMode.FIXED);
            getOffDutyWellsiteLabel().setVisibility(View.VISIBLE);
            getOffDutyWellsiteHoursLabel().setVisibility(View.VISIBLE);
        } else
            mySimpleXYPlot.setRangeBoundaries(0, 4, BoundaryMode.FIXED);

        // get rid of decimal points in our range labels:
        mySimpleXYPlot.setRangeValueFormat(new Format() {
            /**
             * No clue why we need this long, but there was a warning on the "new Format()" without it.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(Object arg0, StringBuffer arg1, FieldPosition arg2) {
                return new StringBuffer("");
            }

            @Override
            public Object parseObject(String arg0, ParsePosition arg1) {
                return null;
            }

        });

        mySimpleXYPlot.setDomainValueFormat(new Format() {
            /**
             * No clue why we need this long, but there was a warning on the "new Format()" without it.
             */
            private static final long serialVersionUID = 1L;

            private SimpleDateFormat dateFormat = new SimpleDateFormat("ha", Locale.getDefault());

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);

                // 6/21/11 JHM - Removed for defect 10494 (grid time labels were wrong)
                //String sAndroidTimeZone = DateUtility.GetAndroidTimeZone(getMyController().getCurrentUser().getHomeTerminalTimeZone());
                //dateFormat.setTimeZone(TimeZone.getTimeZone(sAndroidTimeZone));

                StringBuffer sb = dateFormat.format(date, toAppendTo, pos);
                return sb.deleteCharAt(sb.length() - 1);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });

        // Remove legend and axis labels
        mySimpleXYPlot.getLegendWidget().setVisible(false);
        mySimpleXYPlot.getDomainLabelWidget().setVisible(false);
        mySimpleXYPlot.getRangeLabelWidget().setVisible(false);

        // clear any previous series
        for (XYSeries series : mySimpleXYPlot.getSeriesSet())
            mySimpleXYPlot.removeSeries(series);

        // Split the data into multiple series so we can color code based on Edit state
        SeriesAndFormatterColorCoded seriesAndFormatter = new SeriesAndFormatterColorCoded(data.getDutyStatusValues(), data.getHourValues(),
                data.getIsManuallyEditedByKMBUserValues(),
                data.getIsPCValues(), data.getIsYMValues(), data.getIsPCYMEndValues());
        for (int i = 0; i < seriesAndFormatter.getSeries().size(); i++) {
            mySimpleXYPlot.addSeries(seriesAndFormatter.getSeries().get(i), seriesAndFormatter.getFormatters().get(i));
        }

        // 7/11/11 JHM - Revised boundary settings/limits
        // Relates to changes with grid line not extending to end of grid for current day's log/event
        if (data.getHourValues().size() > 0)
            mySimpleXYPlot.setDomainBoundaries(data.getHourValues().get(0), data.getHourValues().get(0) + 86400, BoundaryMode.FIXED);

        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mySimpleXYPlot.disableAllMarkup();
        getGridImageWrapper().setVisibility(View.VISIBLE);
        mySimpleXYPlot.redraw();
    }

    /**
     * The 'Edit Log' will return Eld Events that have been edited representing by a list of Epoch times.
     * The desire is to draw the ViewGrid but color code the line based on what Eld Events have been edited
     * versus which ones have not been edited.
     * <p>
     * If you try breaking them up into only two separate series (Edit) vs. Non-Edited) they will try to 'connect'
     * missing gaps. My plan here is to group each into multiple series based on it's associated color formatter.
     */
    private class SeriesAndFormatterColorCoded {
        private List<Double> _dutyStatusValues;
        private List<Long> _hourValues;
        private List<Boolean> _isManuallyEditedByKMBUser;
        private List<Boolean> _isPC;
        private List<Boolean> _isYM;
        private List<Boolean> _isPCYMEnd;

        private List<XYSeries> _series = new ArrayList<>();
        private List<LineAndPointFormatter> _formatters = new ArrayList<>();

        private LineAndPointFormatter _standardFormatter;
        private LineAndPointFormatter _editedFormatter;
        private LineAndPointFormatter _pcFormatter;
        private LineAndPointFormatter _ymFormatter;

        public List<XYSeries> getSeries() {
            return _series;
        }

        public List<LineAndPointFormatter> getFormatters() {
            return _formatters;
        }

        public SeriesAndFormatterColorCoded(List<Double> dutyStatusValues, List<Long> hourValues, List<Boolean> isManuallyEditedByKMBUser,
                                            List<Boolean> isPC, List<Boolean> isYM, List<Boolean> isPCYMEnd) {
            _dutyStatusValues = dutyStatusValues;
            _hourValues = hourValues;
            _isManuallyEditedByKMBUser = isManuallyEditedByKMBUser;
            _isPC = isPC;
            _isYM = isYM;
            _isPCYMEnd = isPCYMEnd;

            // Increase the thickness of the red graph line.  The stroke width of 3 made the vertical lines too thick for the phone.  Therefore,
            // we went with a stroke width of 2.
            _standardFormatter = new LineAndPointFormatter(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT);
            Paint standardPaint = _standardFormatter.getLinePaint();
            standardPaint.setStrokeWidth(2);
            _standardFormatter.setLinePaint(standardPaint);

            _editedFormatter = new LineAndPointFormatter(Color.GREEN, Color.TRANSPARENT, Color.TRANSPARENT);
            Paint editedPaint = _editedFormatter.getLinePaint();
            editedPaint.setStrokeWidth(16);
            _editedFormatter.setLinePaint(editedPaint);

            _pcFormatter = new LineAndPointFormatter(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT);
            Paint pcPaint = _pcFormatter.getLinePaint();
            pcPaint.setStrokeWidth(16);
            _pcFormatter.setLinePaint(pcPaint);

            _ymFormatter = new LineAndPointFormatter(Color.BLUE, Color.TRANSPARENT, Color.TRANSPARENT);
            Paint ymPaint = _ymFormatter.getLinePaint();
            ymPaint.setStrokeWidth(16);
            _ymFormatter.setLinePaint(ymPaint);

            SplitSeriesOnEditState();
        }

        private void SplitSeriesOnEditState() {
            List<Long> seriesHourValues = new ArrayList<>();
            List<Double> seriesDutyStatusValues = new ArrayList<>();
            CurrentPointSeries currentPointSeries = CurrentPointSeries.FIRST_POINT;
            for (int i = 0; i < _hourValues.size(); i++) {

                Long startHour = _hourValues.get(i);
                Double startDutyStatus = _dutyStatusValues.get(i);
                Boolean isManuallyEditedByKMBUser = _isManuallyEditedByKMBUser.get(i);
                Boolean isPC = _isPC.get(i);
                Boolean isYM = _isYM.get(i);
                // points are in pairs - start and end duration for each DutyStatus
                i++;

                Long endHour = _hourValues.get(i);
                Double endDutyStatus = _dutyStatusValues.get(i);

                if (isManuallyEditedByKMBUser) {
                    if (currentPointSeries == CurrentPointSeries.EDITED) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "Edited Series"));
                        _formatters.add(_editedFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.NON_EDITED) {
                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.PERSONAL_CONVEYANCE) {
                        // transition is same color as personal conveyance points (Yellow)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "PC Series"));
                        _formatters.add(_pcFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.YARD_MOVE) {
                        // transition is same color as yard move points (Blue)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "YM Series"));
                        _formatters.add(_ymFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    }

                    currentPointSeries = CurrentPointSeries.EDITED;
                } else if (isPC) {
                    if (currentPointSeries == CurrentPointSeries.PERSONAL_CONVEYANCE) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "PC Series"));
                        _formatters.add(_pcFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.EDITED) {
                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "Edited Series"));
                        _formatters.add(_editedFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.NON_EDITED) {
                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.YARD_MOVE) {
                        // transition is same color as yard move points (Blue)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "YM Series"));
                        _formatters.add(_ymFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    }

                    currentPointSeries = CurrentPointSeries.PERSONAL_CONVEYANCE;
                } else if (isYM) {
                    if (currentPointSeries == CurrentPointSeries.YARD_MOVE) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "YM Series"));
                        _formatters.add(_ymFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.EDITED) {
                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "Edited Series"));
                        _formatters.add(_editedFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.NON_EDITED) {
                        // transition is same color as non-edited points (Red)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "NonEdited Series"));
                        _formatters.add(_standardFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    } else if (currentPointSeries == CurrentPointSeries.PERSONAL_CONVEYANCE) {
                        // transition is same color as personal conveyance points (Yellow)
                        seriesHourValues.add(startHour);
                        seriesDutyStatusValues.add(startDutyStatus);

                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "PC Series"));
                        _formatters.add(_pcFormatter);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();
                    }

                    currentPointSeries = CurrentPointSeries.YARD_MOVE;
                } else {
                    if (currentPointSeries == CurrentPointSeries.EDITED) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "Edited Series"));
                        _formatters.add(_editedFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // add transition from last point to current point
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                    } else if (currentPointSeries == CurrentPointSeries.PERSONAL_CONVEYANCE) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "PC Series"));
                        _formatters.add(_pcFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // add transition from last point to current point
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                    } else if (currentPointSeries == CurrentPointSeries.YARD_MOVE) {
                        _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "YM Series"));
                        _formatters.add(_ymFormatter);

                        Long lastHour = seriesHourValues.get(seriesHourValues.size() - 1);
                        Double lastDutyStatus = seriesDutyStatusValues.get(seriesDutyStatusValues.size() - 1);

                        seriesHourValues.clear();
                        seriesDutyStatusValues.clear();

                        // add transition from last point to current point
                        seriesHourValues.add(lastHour);
                        seriesDutyStatusValues.add(lastDutyStatus);
                    }

                    currentPointSeries = CurrentPointSeries.NON_EDITED;
                }

                seriesHourValues.add(startHour);
                seriesDutyStatusValues.add(startDutyStatus);

                seriesHourValues.add(endHour);
                seriesDutyStatusValues.add(endDutyStatus);
            }

            // add the remaining points
            _series.add(new SimpleXYSeries(seriesHourValues, seriesDutyStatusValues, "Sightings in USA"));
            LineAndPointFormatter formatter = _standardFormatter;
            if (currentPointSeries == CurrentPointSeries.EDITED) {
                formatter = _editedFormatter;
            }
            if (currentPointSeries == CurrentPointSeries.PERSONAL_CONVEYANCE) {
                formatter = _pcFormatter;
            }
            if (currentPointSeries == CurrentPointSeries.YARD_MOVE) {
                formatter = _ymFormatter;
            }
            _formatters.add(formatter);
        }
    }

    public enum CurrentPointSeries {
        FIRST_POINT,
        EDITED,
        NON_EDITED,
        PERSONAL_CONVEYANCE,
        YARD_MOVE
    }
}
