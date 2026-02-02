// Debug de l'enregistrement avec différents cas de test
const testCases = [
    {
        name: "Test Simple",
        data: {
            username: "testsimple",
            email: "testsimple@example.com",
            password: "password123",
            role: "STUDENT"
        }
    },
    {
        name: "Test avec nom complexe",
        data: {
            username: "jean_marie_dupont",
            email: "jean.marie@example.com",
            password: "password123",
            role: "TEACHER"
        }
    },
    {
        name: "Test avec caractères spéciaux dans username",
        data: {
            username: "test-user_123",
            email: "testuser123@example.com",
            password: "password123",
            role: "ADMIN"
        }
    }
];

const testRegister = async (testCase) => {
    console.log(`\n=== ${testCase.name} ===`);
    console.log('Data:', testCase.data);
    
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(testCase.data)
        });

        console.log('Status:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('✅ Success:', data);
        } else {
            const error = await response.text();
            console.log('❌ Error:', error);
        }
    } catch (error) {
        console.error('💥 Network error:', error.message);
    }
};

// Exécuter tous les tests
const runAllTests = async () => {
    for (const testCase of testCases) {
        await testRegister(testCase);
        await new Promise(resolve => setTimeout(resolve, 1000)); // Attendre 1 seconde entre les tests
    }
};

runAllTests();