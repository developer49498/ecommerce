import React, { createContext, useContext, useState } from 'react';

const TripRequestContext = createContext();

export function TripRequestProvider({ children }) {
  const [tripRequests, setTripRequests] = useState([
    
    {
      id: 'req1',
      from: 'Koramangala',
      to: 'Indiranagar',
      customer: 'Ananya Sharma',
      phone: '9876543210',
      date: '2025-06-12',
      status: 'Pending',
    },
    {
      id: 'req2',
      from: 'Jayanagar',
      to: 'Whitefield',
      customer: 'Rohan Mehta',
      phone: '9123456780',
      date: '2025-06-12',
      status: 'Pending',
    },
  ]);

  const addTripRequest = (trip) => {
    setTripRequests((prev) => [...prev, { ...trip, id: Date.now().toString(), status: 'Pending' }]);
  };

  const value = { tripRequests, addTripRequest };
  return <TripRequestContext.Provider value={value}>{children}</TripRequestContext.Provider>;
}

export function useTripRequests() {
  return useContext(TripRequestContext);
} 