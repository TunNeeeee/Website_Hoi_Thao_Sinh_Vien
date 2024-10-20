document.addEventListener("DOMContentLoaded", function() {
            const badges = document.querySelectorAll(".badge.bg-red");
            badges.forEach(badge => {
                const startDate = new Date(badge.getAttribute("data-startDate"));
                const endDate = new Date(badge.getAttribute("data-endDate"));
                const now = new Date();

                if (now >= startDate && now <= endDate) {
                    const totalDuration = endDate - startDate;
                    const elapsedDuration = now - startDate;
                    const progress = Math.min((elapsedDuration / totalDuration) * 100, 100);
                    badge.textContent = progress.toFixed(2) + "%";
                } else if (now < startDate) {
                    badge.textContent = "0%";
                } else {
                    badge.textContent = "100%";
                }
            });
        });