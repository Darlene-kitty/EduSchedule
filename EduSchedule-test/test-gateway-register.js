// Test de l'API d'enregistrement via l'API Gateway
const testRegisterViaGateway = async () => {
  try {
    console.log('Testing registration via API Gateway (port 8080)...');
    
    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: 'testgateway',
        email: 'testgateway@example.com',
        password: 'password123',
        role: 'STUDENT'
      })
    });

    console.log('Status:', response.status);
    console.log('Headers:', Object.fromEntries(response.headers.entries()));
    
    if (response.ok) {
      const data = await response.json();
      console.log('Success via Gateway:', data);
    } else {
      const error = await response.text();
      console.log('Error via Gateway:', error);
    }
  } catch (error) {
    console.error('Network error via Gateway:', error);
  }
};

testRegisterViaGateway();