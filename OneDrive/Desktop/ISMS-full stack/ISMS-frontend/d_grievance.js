

const searchGrievance = () => {
    const status = document.getElementById("searchStatus").value;
    const email = localStorage.getItem("email");

    const listEl = document.getElementById("grievanceList");
    const noMsg = document.getElementById("noGrievanceMessage");
    listEl.innerHTML = "";

    let url = "";

    if (status.toLowerCase() === "all" || status.trim() === "") {
        url = `https://13.201.146.238/grievances/by-email/full?email=${encodeURIComponent(email)}`;
    } else {
        url = `https://13.201.146.238/grievances/by-email/status?email=${encodeURIComponent(email)}&status=${encodeURIComponent(status)}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch grievances");
            }
            return response.json();
        })
        .then(result => {
            if (result.length === 0) {
                noMsg.style.display = "block";
            } else {
                
                noMsg.style.display = "none";
                result.forEach(grievance => {
                    const div = document.createElement("div");
                    div.className = "grievance-item";
                    div.setAttribute('data-id', grievance.id);
                    div.innerHTML = `
                        <span class="grievance-id">${grievance.id}</span>
                        <div class="grievance-content">
                            <div class="grievance-subject">${grievance.title}</div>
                            <div class="grievance-meta">
                                <span>${grievance.from.toUpperCase()}</span>
                                ${grievance.forwardedFrom ? `<span class="forwarded-from">Forwarded to ${grievance.forwardedFrom}</span>` : ''}
                                <span>${grievance.formattedTimestamp || ''}</span>
                            </div>
                        </div>
                        <span class="status-badge status-${grievance.status.toLowerCase()}">${grievance.status}</span>
                    `;
                    div.onclick = () => showGrievanceDetails(grievance);
                    listEl.appendChild(div);
                });
            }
        })
        .catch(error => {
            console.error("Error fetching grievances:", error);
            noMsg.style.display = "block";
        });
};

const showGrievanceDetails = async (grievance) => {
    document.getElementById("popupTitle").innerText = grievance.title;
    document.getElementById("popupID").innerText = grievance.id;
    document.getElementById("popupFrom").innerText = grievance.from.toUpperCase();
    document.getElementById("popupTo").innerText = grievance.addressedTo;

    // Forwarded from display
    const forwardedFromRow = document.getElementById("forwardedFromRow");
    const popupForwardedFrom = document.getElementById("popupForwardedFrom");
    const forwardingChainRow = document.getElementById("forwardingChainRow");
    const forwardingChain = document.getElementById("forwardingChain");

    if (grievance.forwardingChain && grievance.forwardingChain.length > 0) {
        forwardedFromRow.style.display = "grid";
        const latestForward = grievance.forwardingChain[grievance.forwardingChain.length - 1];
        popupForwardedFrom.innerText = latestForward.to;
    } else {
        forwardedFromRow.style.display = "none";
    }

    // Show forwarding chain if it exists
    if (grievance.forwarding_chain) {
        forwardingChainRow.style.display = "grid";

        const forward = grievance.forwarding_chain;

        const [fromNotation, toNotation] = await Promise.all([
            getNotationName(forward.forwardedFrom),
            getNotationName(forward.forwardedTo)
        ]);

        forwardingChain.innerHTML = `
            <div class="forwarding-chain-item">
                <div class="forward-details">
                    <div>
                        <span class="forward-from">${fromNotation}</span>
                        <span class="forward-arrow">→</span>
                        <span class="forward-to">${toNotation}</span>
                    </div>
                    <div class="forward-time">${forward.formattedForwardedTime || new Date(forward.forwardedTime).toLocaleString()}</div>
                    <div class="forward-reason">${forward.forwardingNote || ""}</div>
                </div>
            </div>
        `;
    } else {
        forwardingChainRow.style.display = "none";
        forwardingChain.innerHTML = '';
    }


    window.currentGrievance = grievance;

    // Populate forwardTo dropdown dynamically
    const forwardToSelect = document.getElementById("forwardTo");
    forwardToSelect.innerHTML = `<option value="">Select official</option>`; // reset

    try {
        const response = await fetch("https://13.201.146.238/officials/notations");
        const allNotations = await response.json();

        const currentUserEmail = grievance.resolveAccess?.trim(); // person currently handling
        const currentUserNotation = await getNotationName(currentUserEmail);

        const filteredNotations = allNotations.filter(n => n !== currentUserNotation);

        filteredNotations.forEach(notation => {
            const option = document.createElement("option");
            option.value = notation;
            option.textContent = notation;
            forwardToSelect.appendChild(option);
        });
    } catch (error) {
        console.error("Failed to load forward-to options:", error);
    }

    setupForwardChainUI(grievance);

    document.getElementById("popupDateTime").innerText = grievance.formattedTimestamp || "";
    document.getElementById("popupSubject").innerText = grievance.title;
    document.getElementById("popupDescription").innerText = grievance.content;

    const statusElement = document.getElementById("popupStatus");
    statusElement.innerText = grievance.status;
    statusElement.className = `status-badge status-${grievance.status.toLowerCase()}`;

    const resolveBtn = document.querySelector(".resolve-btn");
    const forwardChainBtn = document.getElementById("forwardChainBtn");

    const status = grievance.status.toLowerCase();

    if (status === "pending") {
        resolveBtn.style.display = "block";
        forwardChainBtn.style.display = "inline-flex";
    } else {
        resolveBtn.style.display = "none";
        forwardChainBtn.style.display = "none";
    }

    document.getElementById("grievancePopup").style.display = "flex";
    document.getElementById("popupOverlay").style.display = "block";
};




const populateForwardDropdown = async (currentNotation) => {
    const dropdown = document.getElementById("forwardChainDropdown");
    dropdown.innerHTML = ""; // clear old options

    try {
        const response = await fetch("https://13.201.146.238/officials/notations");
        const notations = await response.json();

        const filteredNotations = notations.filter(n => n !== currentNotation);

        filteredNotations.forEach(notation => {
            const option = document.createElement("div");
            option.className = "dropdown-option";
            option.dataset.value = notation;
            option.innerText = notation;
            option.addEventListener("click", () => {
                dropdown.style.display = "none";
                document.getElementById("forwardReasonSection").style.display = "block";
                dropdown.setAttribute("data-selected", notation);
            });
            dropdown.appendChild(option);
        });
    } catch (err) {
        console.error("Failed to fetch notations:", err);
        dropdown.innerHTML = "<div class='dropdown-option'>Error loading options</div>";
    }
};


document.getElementById("forwardChainBtn").addEventListener("click", async () => {
    const dropdown = document.getElementById("forwardChainDropdown");
    dropdown.style.display = "block";

    const grievance = window.currentGrievance; // this should be set during showGrievanceDetails
    await populateForwardDropdown(grievance.addressedTo);
});

const getNotationName = async (email) => {
    try {
        const response = await fetch(`https://13.201.146.238/officials/notation?email=${encodeURIComponent(email.trim())}`);
        if (!response.ok) throw new Error("Failed to fetch notation name");
        
        // Use text() instead of json()
        const notationName = await response.text();
        return notationName || email;
    } catch (error) {
        console.error("Error fetching notation name:", error);
        return email; // fallback to email if error
    }
};

document.getElementById("forwardChainConfirmBtn").addEventListener("click", async () => {
    const grievanceId = document.getElementById("popupID").innerText;
    const forwardingNote = document.getElementById("forwardChainReason").value.trim();
    const toNotationName = document.getElementById("forwardChainDropdown").getAttribute("data-selected");
    const email = localStorage.getItem("email");

    if (!toNotationName || !forwardingNote) {
        alert("Please select a recipient and enter a reason for forwarding.");
        return;
    }

    try {
        const fromNotationName = await getNotationName(email);

        const url = new URL("https://13.201.146.238/grievances/forward");
        url.searchParams.append("grievanceId", grievanceId);
        url.searchParams.append("fromNotationName", fromNotationName);
        url.searchParams.append("toNotationName", toNotationName);
        url.searchParams.append("forwardingNote", forwardingNote);

        const response = await fetch(url.toString(), {
            method: "POST"
        });

        if (response.ok) {
            alert("Grievance forwarded successfully!");
            document.getElementById("grievancePopup").style.display = "none";
            document.getElementById("popupOverlay").style.display = "none";
            // Optionally refresh your list here
        } else {
            alert("Failed to forward grievance.");
        }
    } catch (err) {
        console.error("Error during forwarding:", err);
        alert("Something went wrong. Try again.");
    }
});



document.querySelector(".resolve-btn").addEventListener("click", async () => {
    const grievanceId = document.getElementById("popupID").innerText;
    const email = localStorage.getItem("email");

    if (!email) {
        alert("User not logged in.");
        return;
    }

    try {
        const resolverNotationName = await getNotationName(email);

        const url = new URL("https://13.201.146.238/grievances/resolve");
        url.searchParams.append("grievanceId", grievanceId);
        url.searchParams.append("resolverNotationName", resolverNotationName);

        const response = await fetch(url.toString(), {
            method: "POST"
        });

        if (response.ok) {
            alert("Grievance resolved successfully!");
            document.getElementById("grievancePopup").style.display = "none";
            document.getElementById("popupOverlay").style.display = "none";
            // Optionally refresh grievance list here
        } else {
            alert("Failed to resolve grievance.");
        }
    } catch (err) {
        console.error("Error resolving grievance:", err);
        alert("Something went wrong. Please try again.");
    }
});


const closePopup = () => {
    document.getElementById("grievancePopup").style.display = "none";
    document.getElementById("popupOverlay").style.display = "none";
};

document.getElementById("menuToggle").addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("show");
});

document.addEventListener("click", (e) => {
    const sidebar = document.getElementById("sidebar");
    const toggle = document.getElementById("menuToggle");
    if (window.innerWidth <= 768 && !sidebar.contains(e.target) && !toggle.contains(e.target)) {
        sidebar.classList.remove("show");
    }
});

// Initial load
searchGrievance();

// Add event listeners for the new forward chain button and dropdown
function setupForwardChainUI(grievance) {
    const forwardChainBtn = document.getElementById('forwardChainBtn');
    const forwardChainDropdown = document.getElementById('forwardChainDropdown');
    const forwardReasonSection = document.getElementById('forwardReasonSection');
    const forwardChainReason = document.getElementById('forwardChainReason');
    const forwardChainConfirmBtn = document.getElementById('forwardChainConfirmBtn');

    // Hide dropdown and reason section initially
    forwardChainDropdown.classList.remove('active');
    forwardReasonSection.style.display = 'none';
    forwardChainReason.value = '';

    let selectedAuthority = '';

    // Toggle dropdown
    forwardChainBtn.onclick = function(e) {
        e.stopPropagation();
        forwardChainDropdown.classList.toggle('active');
        forwardChainBtn.classList.toggle('open');
        forwardReasonSection.style.display = 'none';
    };

    // Hide dropdown if clicking outside
    document.addEventListener('click', function hideDropdown(e) {
        if (!forwardChainBtn.contains(e.target) && !forwardChainDropdown.contains(e.target)) {
            forwardChainDropdown.classList.remove('active');
            forwardChainBtn.classList.remove('open');
            forwardReasonSection.style.display = 'none';
        }
    }, { once: true });

    // Handle dropdown option click
    Array.from(forwardChainDropdown.getElementsByClassName('dropdown-option')).forEach(option => {
        option.onclick = function(e) {
            selectedAuthority = option.getAttribute('data-value');
            forwardChainDropdown.classList.remove('active');
            forwardReasonSection.style.display = 'flex';
            forwardChainReason.value = '';
        };
    });

    // Handle confirm forward
    forwardChainConfirmBtn.onclick = function() {
        const reason = forwardChainReason.value.trim();
        if (!selectedAuthority) {
            alert('Please select an authority to forward to.');
            return;
        }
        if (!reason) {
            alert('Please provide a reason for forwarding.');
            return;
        }
        // Add to forwarding chain
        grievance.forwardingChain.push({
            from: grievance.to,
            to: selectedAuthority.charAt(0).toUpperCase() + selectedAuthority.slice(1),
            reason: reason,
            timestamp: new Date().toISOString()
        });
        grievance.forwardedFrom = grievance.to;
        grievance.to = selectedAuthority.charAt(0).toUpperCase() + selectedAuthority.slice(1);
        grievance.status = 'Forwarded';
        // Update the status badge in the list
        const statusBadge = document.querySelector(`.grievance-item[data-id="${grievance.id}"] .status-badge`);

        if (statusBadge) {
            statusBadge.textContent = 'Forwarded';
            statusBadge.className = 'status-badge status-forwarded';
        }

        // Refresh popup
        showGrievanceDetails(grievance);
        // Hide reason section
        forwardReasonSection.style.display = 'none';
        // Refresh list
        searchGrievance();
    };
}


