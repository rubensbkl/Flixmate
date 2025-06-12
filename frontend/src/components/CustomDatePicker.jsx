"use client";

import { useState } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

export default function CustomDatePicker({ value, onChange }) {
    const [selectedDate, setSelectedDate] = useState(value);

    const handleDateChange = (date) => {
        setSelectedDate(date);
        onChange(date);
    };

    return (
        <DatePicker
            selected={selectedDate}
            onChange={handleDateChange}
            dateFormat="dd/MM/yyyy"
            placeholderText="Data de Nascimento"
            className="w-full p-3 bg-foreground border border-foreground rounded-lg text-primary placeholder-secondary focus:border-accent focus:outline-none transition-colors"
            maxDate={new Date()}
            showYearDropdown
            scrollableYearDropdown
        />
    );
}