"use client";

import React, { useEffect, useState } from 'react';
import SearchIcon from '@mui/icons-material/Search';

const Searchbar = ({ onSearch, initialValue = '', placeholder = '' }) => {

    const [value, setValue] = useState(initialValue);

    useEffect(() => {
        setValue(initialValue);
    }, [initialValue]);

    const handleInputChange = (e) => {
        const newValue = e.target.value;
        setValue(newValue);
        if (onSearch) {
            onSearch(newValue);
        }
    };

    return (
        <div className="w-full max-w-lg mx-auto bg-foreground rounded-xl px-4 py-2 flex items-center gap-2">
            <SearchIcon className="text-secondary" />
            <input
                type="text"
                placeholder={placeholder}
                value={value}
                onChange={handleInputChange}
                className="w-full bg-transparent outline-none text-primary placeholder-secondary"
            />
        </div>
    );
};

export default Searchbar;
