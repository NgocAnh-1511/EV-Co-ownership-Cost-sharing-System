document.addEventListener('DOMContentLoaded', async () => {
    const username = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    if (!username || !token) {
        alert('Vui lòng đăng nhập lại!');
        window.location.href = '/admin-login';
        return;
    }
    document.getElementById('username').textContent = username || 'Admin';

    async function fetchData(url, tableId, renderFunction) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) throw new Error(`Lỗi: ${response.status} - ${await response.text()}`);
            const data = await response.json();
            renderFunction(data);
        } catch (error) {
            console.error(`Lỗi fetch từ ${url}:`, error);
            document.querySelector(`#${tableId} tbody`).innerHTML = `<tr><td colspan="4">Lỗi: ${error.message}</td></tr>`;
        }
    }

    function renderVehicleTable(vehicles) {
        const tbody = document.querySelector('#vehicleTable tbody');
        tbody.innerHTML = '';
        if (!vehicles || vehicles.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">Không có dữ liệu xe.</td></tr>';
            return;
        }
        vehicles.forEach(v => {
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td>${v.vehicle_id || '-'}</td>
                    <td>${v.vehicle_name || '-'}</td>
                    <td>${v.license_plate || '-'}</td>
                    <td>${v.status || '-'}</td>
                </tr>
            `);
        });
    }

    function renderReservationTable(reservations) {
        const tbody = document.querySelector('#reservationTable tbody');
        tbody.innerHTML = '';
        if (!reservations || reservations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">Không có dữ liệu đặt lịch.</td></tr>';
            return;
        }
        reservations.forEach(r => {
            tbody.insertAdjacentHTML('beforeend', `
                <tr>
                    <td>${r.reservation_id || '-'}</td>
                    <td>${r.vehicle?.vehicle_id || '-'}</td>
                    <td>${r.user?.username || '-'}</td>
                    <td>${r.start_datetime || '-'}</td>
                    <td>${r.end_datetime || '-'}</td>
                    <td>${r.status || '-'}</td>
                </tr>
            `);
        });
    }

    await fetchData('http://localhost:8082/api/admin/vehicles', 'vehicleTable', renderVehicleTable);
    await fetchData('http://localhost:8082/api/admin/reservations', 'reservationTable', renderReservationTable);

    document.getElementById('logout').addEventListener('click', () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        window.location.href = '/admin-login';
    });
});