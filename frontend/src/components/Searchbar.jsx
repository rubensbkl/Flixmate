import React from 'react';
import SearchIcon from '@mui/icons-material/Search';

const Searchbar = ({ onSearch }) => {
  const handleInputChange = (e) => {
    if (onSearch) {
      onSearch(e.target.value);
    }
  };

  return (
    <div className="w-full max-w-lg mx-auto bg-foreground rounded-xl px-4 py-2 flex items-center gap-2">
      <SearchIcon className="text-secondary" />
      <input
        type="text"
        placeholder="Buscar usuário..."
        onChange={handleInputChange}
        className="w-full bg-transparent outline-none text-primary placeholder-secondary"
      />
    </div>
  );
};

export default Searchbar;