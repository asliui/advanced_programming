<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Movies Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; vertical-align: top; }
        th { background: #f3f3f3; }
        .small { color: #666; font-size: 0.9em; }
        .nowrap { white-space: nowrap; }
    </style>
</head>
<body>
<h1>Movies Report</h1>
<div class="small">Source: database view <code>v_movie_report</code></div>

<table>
    <thead>
    <tr>
        <th class="nowrap">Movie ID</th>
        <th>Title</th>
        <th class="nowrap">Release Date</th>
        <th class="nowrap">Duration</th>
        <th class="nowrap">Score</th>
        <th>Genre</th>
        <th>Actors</th>
    </tr>
    </thead>
    <tbody>
    <#list movies as m>
        <tr>
            <td class="nowrap">${m.movieId}</td>
            <td>${m.title}</td>
            <td class="nowrap">${m.releaseDate}</td>
            <td class="nowrap">${m.duration}</td>
            <td class="nowrap">${m.score}</td>
            <td>${m.genreName}</td>
            <td>${m.actors}</td>
        </tr>
    </#list>
    </tbody>
</table>
</body>
</html>

