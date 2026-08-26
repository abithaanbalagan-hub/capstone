import { useState } from 'react'
import './App.css'
import Login from './Login'
import Register from './Register'

function App() {
  const [page, setPage] = useState('login')

  return (
    <>
      {page === 'login' ? (
        <Login onRegister={() => setPage('register')} />
      ) : (
        <Register onLogin={() => setPage('login')} />
      )}
    </>
  )
}

export default App