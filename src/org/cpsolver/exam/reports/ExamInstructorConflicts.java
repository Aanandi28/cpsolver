package org.cpsolver.exam.reports;

import org.cpsolver.exam.criteria.InstructorBackToBackConflicts;
import org.cpsolver.exam.criteria.InstructorDistanceBackToBackConflicts;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamModel;
import org.cpsolver.exam.model.ExamOwner;
import org.cpsolver.exam.model.ExamPeriod;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.CSVFile.CSVField;

/**
 * Export instructor direct, back-to-back, and more than two exams a day
 * conflicts into a CSV file. <br>
 * <br>
 * Usage:
 * 
 * <pre>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;new ExamInstructorConflicts(model).report().save(file);
 * </code>
 * </pre>
 * 
 * <br>
 *
 * @version ExamTT 1.3 (Examination Timetabling)<br>
 *          Copyright (C) 2008 - 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href=
 *          "http://muller.unitime.org">http://muller.unitime.org</a><br>
 *          <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 *          <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 *          <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see <a href=
 *          'http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class ExamInstructorConflicts {
    private ExamModel iModel = null;

    /**
     * Constructor
     *
     * @param model
     *                  examination timetabling model
     */
    public ExamInstructorConflicts(ExamModel model) {
        iModel = model;
    }

    /**
     * generate report
     * 
     * @param assignment
     *                       current assignment
     * @return resultant report
     */
    public CSVFile report(Assignment<Exam, ExamPlacement> assignment) {
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVField[] { new CSVField("Instructor"), new CSVField("Type"), new CSVField("Section/Course"),
                new CSVField("Period"), new CSVField("Day"), new CSVField("Time"), new CSVField("Room"),
                new CSVField("Distance") });
        boolean isDayBreakBackToBack = ((InstructorBackToBackConflicts) iModel
                .getCriterion(InstructorBackToBackConflicts.class)).isDayBreakBackToBack();
        double backToBackDistance = ((InstructorDistanceBackToBackConflicts) iModel
                .getCriterion(InstructorDistanceBackToBackConflicts.class)).getBackToBackDistance();
        for (ExamInstructor instructor : iModel.getInstructors()) {
            for (ExamPeriod period : iModel.getPeriods()) {
                int numberOfExams = instructor.getExams(assignment, period).size();
                if (numberOfExams > 1) {
                    String sections = "";
                    String rooms = "";
                    String periods = String.valueOf(period.getIndex() + 1);
                    String periodDays = period.getDayStr();
                    String periodTimes = period.getTimeStr();

                    for (Exam exam : instructor.getExams(assignment, period)) {
                        ExamPlacement placement = assignment.getValue(exam);
                        String roomsThisExam = "";
                        for (ExamRoomPlacement room : placement.getRoomPlacements()) {
                            if (roomsThisExam.length() > 0)
                                roomsThisExam += ", ";
                            roomsThisExam += room.getName();
                        }
                        boolean first = true;
                        for (ExamOwner owner : exam.getOwners(instructor)) {
                            if (sections.length() > 0) {
                                sections += "\n";
                                rooms += "\n";
                                periods += "\n";
                                periodDays += "\n";
                                periodTimes += "\n";
                            }
                            sections += owner.getName();
                            if (first)
                                rooms += roomsThisExam;
                            first = false;
                        }
                        if (exam.getOwners(instructor).isEmpty()) {
                            sections += exam.getName();
                            rooms += roomsThisExam;
                        }
                    }
                    csv.addLine(new CSVField[] { new CSVField(instructor.getName()), new CSVField("direct"),
                            new CSVField(sections), new CSVField(periods), new CSVField(periodDays),
                            new CSVField(periodTimes), new CSVField(rooms) });
                }
                if (numberOfExams > 0) {
                    if (period.next() != null && !instructor.getExams(assignment, period.next()).isEmpty()
                            && (!isDayBreakBackToBack || period.next().getDay() == period.getDay())) {
                        for (Exam firstExam : instructor.getExams(assignment, period)) {
                            for (Exam secondExam : instructor.getExams(assignment, period.next())) {
                                ExamPlacement placement = assignment.getValue(firstExam);
                                String sections = "";
                                String rooms = "";
                                String roomsThisExam = "";
                                String periods = String.valueOf(period.getIndex() + 1);
                                String periodDays = period.getDayStr();
                                String periodTimes = period.getTimeStr();
                                for (ExamRoomPlacement room : placement.getRoomPlacements()) {
                                    if (roomsThisExam.length() > 0)
                                        roomsThisExam += ", ";
                                    roomsThisExam += room.getName();
                                }
                                boolean first = true;
                                for (ExamOwner owner : firstExam.getOwners(instructor)) {
                                    if (sections.length() > 0) {
                                        sections += "\n";
                                        rooms += "\n";
                                        periods += "\n";
                                        periodDays += "\n";
                                        periodTimes += "\n";
                                    }
                                    sections += owner.getName();
                                    if (first)
                                        rooms += roomsThisExam;
                                    first = false;
                                }
                                if (firstExam.getOwners(instructor).isEmpty()) {
                                    sections += firstExam.getName();
                                    rooms += roomsThisExam;
                                }
                                placement = assignment.getValue(secondExam);
                                roomsThisExam = "";
                                for (ExamRoomPlacement room : placement.getRoomPlacements()) {
                                    if (roomsThisExam.length() > 0)
                                        roomsThisExam += ", ";
                                    roomsThisExam += room.getName();
                                }
                                first = true;
                                for (ExamOwner owner : secondExam.getOwners(instructor)) {
                                    sections += "\n";
                                    rooms += "\n";
                                    periods += "\n";
                                    periodDays += "\n";
                                    periodTimes += "\n";
                                    sections += owner.getName();

                                    if (first) {
                                        rooms += roomsThisExam;
                                        periods += String.valueOf(period.next().getIndex() + 1);
                                        periodDays += period.next().getDayStr();
                                        periodTimes += period.next().getTimeStr();
                                    }
                                    first = false;
                                }
                                if (secondExam.getOwners(instructor).isEmpty()) {
                                    sections += "\n";
                                    rooms += "\n";
                                    periods += "\n";
                                    periodDays += "\n";
                                    periodTimes += "\n";
                                    sections += secondExam.getName();
                                    rooms += roomsThisExam;
                                    periods += String.valueOf(period.next().getIndex() + 1);
                                    periodDays += period.next().getDayStr();
                                    periodTimes += period.next().getTimeStr();
                                }
                                String distanceString = "";
                                if (backToBackDistance >= 0) {
                                    double gapBetweenTwoExams = (assignment.getValue(firstExam))
                                            .getDistanceInMeters(assignment.getValue(secondExam));
                                    if (gapBetweenTwoExams > 0)
                                        distanceString = String.valueOf(gapBetweenTwoExams);
                                }
                                csv.addLine(new CSVField[] { new CSVField(instructor.getName()),
                                        new CSVField("back-to-back"), new CSVField(sections), new CSVField(periods),
                                        new CSVField(periodDays), new CSVField(periodTimes), new CSVField(rooms),
                                        new CSVField(distanceString) });
                            }
                        }
                    }
                }
                if (period.next() == null || period.next().getDay() != period.getDay()) {
                    int nrExamsADay = instructor.getExamsADay(assignment, period.getDay()).size();
                    if (nrExamsADay > 2) {
                        String sections = "";
                        String periods = "";
                        String periodDays = "";
                        String periodTimes = "";
                        String rooms = "";
                        for (Exam exam : instructor.getExamsADay(assignment, period.getDay())) {
                            ExamPlacement placement = assignment.getValue(exam);
                            String roomsThisExam = "";
                            for (ExamRoomPlacement room : placement.getRoomPlacements()) {
                                if (roomsThisExam.length() > 0)
                                    roomsThisExam += ", ";
                                roomsThisExam += room.getName();
                            }
                            boolean first = true;
                            ExamPeriod placementPeriod = placement.getPeriod();
                            for (ExamOwner owner : exam.getOwners(instructor)) {
                                if (sections.length() > 0) {
                                    sections += "\n";
                                    rooms += "\n";
                                    periods += "\n";
                                    periodDays += "\n";
                                    periodTimes += "\n";
                                }
                                sections += owner.getName();
                                if (first) {
                                    periods += (placementPeriod.getIndex() + 1);
                                    periodDays += placementPeriod.getDayStr();
                                    periodTimes += placementPeriod.getTimeStr();
                                    rooms += roomsThisExam;
                                }
                                first = false;
                            }
                            if (exam.getOwners(instructor).isEmpty()) {
                                if (sections.length() > 0) {
                                    sections += "\n";
                                    rooms += "\n";
                                    periods += "\n";
                                    periodDays += "\n";
                                    periodTimes += "\n";
                                }
                                sections += exam.getName();
                                periods += (placementPeriod.getIndex() + 1);
                                periodDays += placementPeriod.getDayStr();
                                periodTimes += placementPeriod.getTimeStr();
                                rooms += roomsThisExam;
                            }
                        }
                        csv.addLine(new CSVField[] { new CSVField(instructor.getName()), new CSVField("more-2-day"),
                                new CSVField(sections), new CSVField(periods), new CSVField(periodDays),
                                new CSVField(periodTimes), new CSVField(rooms) });
                    }
                }
            }
        }
        return csv;
    }
}