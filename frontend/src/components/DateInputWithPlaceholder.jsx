import React, { forwardRef } from 'react';
import DatePicker, { registerLocale } from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import ptBR from 'date-fns/locale/pt-BR';
import { IMaskInput } from 'react-imask';

registerLocale('pt-BR', ptBR);

const MaskedInput = forwardRef((props, ref) => (
    <IMaskInput
        {...props}
        inputRef={ref}
        mask="00/00/0000"
    />
));
MaskedInput.displayName = 'MaskedInput';

export function DateInputWithPlaceholder({ 
    placeholder = "Data de nascimento", 
    value, 
    onChange, 
    name,
    className = "",
    min,
    max,
    ...props 
}) {
    // value is expected to be 'YYYY-MM-DD' from the parent state
    // We need to parse it to a Date object for DatePicker, and then send back 'YYYY-MM-DD' on change.
    
    let selectedDate = null;
    if (value) {
        // Fix timezone issues by creating date with T00:00:00
        selectedDate = new Date(value + 'T00:00:00');
    }

    const handleChange = (date) => {
        if (!date) {
            onChange({ target: { name, value: "" } });
            return;
        }
        
        // Format to YYYY-MM-DD to keep compatibility with parent
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const formattedDate = `${year}-${month}-${day}`;
        
        onChange({ target: { name, value: formattedDate } });
    };

    return (
        <div className="relative w-full">
            <DatePicker
                selected={selectedDate}
                onChange={handleChange}
                dateFormat="dd/MM/yyyy"
                locale="pt-BR"
                placeholderText={placeholder}
                showMonthDropdown
                showYearDropdown
                dropdownMode="select"
                minDate={min ? new Date(min + 'T00:00:00') : null}
                maxDate={max ? new Date(max + 'T00:00:00') : null}
                customInput={<MaskedInput />}
                className={`
                    w-full p-4 bg-background/50 border border-white/5 rounded-xl 
                    focus:border-accent focus:ring-1 focus:ring-accent focus:bg-background/80 
                    outline-none transition-all duration-200
                    ${value ? 'text-primary' : 'text-secondary/50 placeholder-secondary/50'}
                    ${className}
                `}
                name={name}
                {...props}
            />
            {/* Calendar Icon wrapper */}
            <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-secondary">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            </div>
        </div>
    );
}