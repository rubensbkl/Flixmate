"use client";

import { useState } from 'react';

export function DateInputWithPlaceholder({ 
    placeholder = "Data de nascimento", 
    value, 
    onChange, 
    name,
    className = "",
    ...props 
}) {
    const [inputType, setInputType] = useState('text');

    const handleFocus = () => {
        setInputType('date');
    };

    const handleBlur = (e) => {
        if (!e.target.value) {
            setInputType('text');
        }
    };

    return (
        <input
            type={inputType}
            placeholder={inputType === 'text' ? placeholder : ''}
            className={`
                w-full p-3 bg-foreground border border-foreground rounded-lg 
                focus:border-accent focus:outline-none transition-colors
                ${value ? 'text-primary' : 'text-secondary'}
                placeholder-secondary
                ${className}
            `}
            value={value}
            onChange={onChange}
            onFocus={handleFocus}
            onBlur={handleBlur}
            name={name}
            {...props}
        />
    );
}