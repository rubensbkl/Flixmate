"use client";

import {
  Bars3BottomLeftIcon,
  CalendarDaysIcon,
  ChevronDownIcon,
  FunnelIcon,
  TagIcon,
  XMarkIcon
} from '@heroicons/react/24/outline';
import { useEffect, useState } from 'react';

const AdvancedFilters = ({ onApplyFilters, initialFilters = {} }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isMounted, setIsMounted] = useState(false);
  const [filters, setFilters] = useState({
    sortBy: 'popularity',
    genres: [],
    yearFrom: '',
    yearTo: '',
    ...initialFilters
  });

  // Fix para SSR hydration
  useEffect(() => {
    setIsMounted(true);
  }, []);

  // Se ainda não foi montado no cliente, não renderizar o dropdown
  if (!isMounted) {
    return (
      <button className="flex items-center gap-2 px-4 py-2 rounded-lg border bg-background text-primary border-foreground">
        <FunnelIcon className="w-4 h-4" />
        <span className="font-medium">Filtros Avançados</span>
      </button>
    );
  }

  

    // Lista de gêneros baseada no seu banco de dados
  const genres = [
    { id: 28, name: 'Ação' },
    { id: 12, name: 'Aventura' },
    { id: 16, name: 'Animação' },
    { id: 35, name: 'Comédia' },
    { id: 80, name: 'Crime' },
    { id: 99, name: 'Documentário' },
    { id: 18, name: 'Drama' },
    { id: 10751, name: 'Família' },
    { id: 14, name: 'Fantasia' },
    { id: 36, name: 'História' },
    { id: 27, name: 'Terror' },
    { id: 10402, name: 'Música' },
    { id: 9648, name: 'Mistério' },
    { id: 10749, name: 'Romance' },
    { id: 878, name: 'Ficção científica' },
    { id: 10770, name: 'Cinema TV' },
    { id: 53, name: 'Thriller' },
    { id: 10752, name: 'Guerra' },
    { id: 37, name: 'Faroeste' }
  ];

  const sortOptions = [
    { value: 'popularity', label: 'Popularidade' },
    { value: 'rating', label: 'Avaliação' },
    { value: 'release_date_desc', label: 'Mais recentes' },
    { value: 'release_date_asc', label: 'Mais antigos' },
    { value: 'title', label: 'Título (A-Z)' }
  ];

  const handleGenreToggle = (genreId) => {
    setFilters(prev => ({
      ...prev,
      genres: prev.genres.includes(genreId)
        ? prev.genres.filter(id => id !== genreId)
        : [...prev.genres, genreId]
    }));
  };

  const handleApplyFilters = () => {
    onApplyFilters(filters);
    setIsOpen(false);
  };

  const handleClearFilters = () => {
    const clearedFilters = {
      sortBy: 'popularity',
      genres: [],
      yearFrom: '',
      yearTo: ''
    };
    setFilters(clearedFilters);
    onApplyFilters(clearedFilters);
  };

  const hasActiveFilters = () => {
    return filters.genres.length > 0 || 
           filters.yearFrom !== '' || 
           filters.yearTo !== '' || 
           filters.sortBy !== 'popularity';
  };

  return (
    <div className="relative">
      {/* Botão para abrir filtros */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-all duration-200 ${
          hasActiveFilters()
            ? 'bg-accent text-background border-accent shadow-lg'
            : 'bg-background text-primary border-foreground hover:bg-foreground hover:border-accent'
        }`}
      >
        <FunnelIcon className="w-4 h-4" />
        <span className="font-medium">Filtros Avançados</span>
        {hasActiveFilters() && (
          <span className="bg-background text-accent px-2 py-0.5 rounded-full text-xs font-bold">
            {filters.genres.length + (filters.yearFrom ? 1 : 0) + (filters.yearTo ? 1 : 0) + (filters.sortBy !== 'popularity' ? 1 : 0)}
          </span>
        )}
        <ChevronDownIcon className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {/* Painel de filtros */}
      {isOpen && (
        <>
          {/* Backdrop para mobile */}
          <div 
            className="fixed inset-0 bg-black/50 z-[60] md:hidden"
            onClick={() => setIsOpen(false)}
          />
          
          {/* Modal/Dropdown */}
          <div className={`
            bg-background border border-foreground rounded-xl shadow-2xl overflow-hidden z-[70]
            md:absolute md:top-full md:left-0 md:mt-2 md:w-96 md:max-h-[80vh]
            fixed inset-4 top-16 bottom-4 md:inset-auto flex flex-col
          `}>
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-foreground bg-foreground/50 flex-shrink-0">
              <h3 className="text-lg font-semibold text-primary flex items-center gap-2">
                <FunnelIcon className="w-5 h-5" />
                Filtros Avançados
              </h3>
              <button
                onClick={() => setIsOpen(false)}
                className="text-secondary hover:text-primary transition-colors p-1 hover:bg-foreground rounded-lg"
              >
                <XMarkIcon className="w-5 h-5" />
              </button>
            </div>

            {/* Conteúdo scrollável */}
            <div className="p-4 space-y-6 overflow-y-auto flex-1">
              {/* Ordenação */}
              <div className="space-y-3">
                <label className="flex items-center gap-2 text-sm font-medium text-primary">
                  <Bars3BottomLeftIcon className="w-4 h-4" />
                  Ordenar por
                </label>
                <select
                  value={filters.sortBy}
                  onChange={(e) => setFilters(prev => ({ ...prev, sortBy: e.target.value }))}
                  className="w-full px-3 py-2.5 bg-foreground border border-foreground rounded-lg text-primary focus:outline-none focus:ring-2 focus:ring-accent focus:border-accent transition-all"
                >
                  {sortOptions.map(option => (
                    <option key={option.value} value={option.value} className="bg-background">
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Filtro por ano */}
              <div className="space-y-3">
                <label className="flex items-center gap-2 text-sm font-medium text-primary">
                  <CalendarDaysIcon className="w-4 h-4" />
                  Ano de lançamento
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <input
                    type="number"
                    placeholder="De (ex: 2020)"
                    value={filters.yearFrom}
                    onChange={(e) => setFilters(prev => ({ ...prev, yearFrom: e.target.value }))}
                    className="w-full px-3 py-2.5 bg-foreground border border-foreground rounded-lg text-primary placeholder-secondary focus:outline-none focus:ring-2 focus:ring-accent focus:border-accent transition-all"
                    min="1900"
                    max="2030"
                  />
                  <input
                    type="number"
                    placeholder="Até (ex: 2024)"
                    value={filters.yearTo}
                    onChange={(e) => setFilters(prev => ({ ...prev, yearTo: e.target.value }))}
                    className="w-full px-3 py-2.5 bg-foreground border border-foreground rounded-lg text-primary placeholder-secondary focus:outline-none focus:ring-2 focus:ring-accent focus:border-accent transition-all"
                    min="1900"
                    max="2030"
                  />
                </div>
              </div>

              {/* Filtro por gênero */}
              <div className="space-y-3">
                <label className="flex items-center gap-2 text-sm font-medium text-primary">
                  <TagIcon className="w-4 h-4" />
                  Gêneros ({filters.genres.length} selecionados)
                </label>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {genres.map(genre => (
                    <button
                      key={genre.id}
                      onClick={() => handleGenreToggle(genre.id)}
                      className={`px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 text-center ${
                        filters.genres.includes(genre.id)
                          ? 'bg-accent text-background shadow-md'
                          : 'bg-foreground text-secondary hover:bg-foreground/80 hover:text-primary'
                      }`}
                    >
                      {genre.name}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Footer com botões - sempre visível */}
            <div className="flex gap-3 p-4 border-t border-foreground bg-foreground/90 backdrop-blur-sm flex-shrink-0">
              <button
                onClick={handleClearFilters}
                className="flex-1 px-4 py-2.5 text-secondary hover:text-primary border border-foreground rounded-lg transition-all duration-200 hover:bg-foreground font-medium"
              >
                Limpar
              </button>
              <button
                onClick={handleApplyFilters}
                className="flex-1 px-4 py-2.5 bg-accent text-background rounded-lg font-medium transition-all duration-200 hover:bg-accent/90 shadow-lg hover:shadow-xl"
              >
                Aplicar Filtros
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default AdvancedFilters;