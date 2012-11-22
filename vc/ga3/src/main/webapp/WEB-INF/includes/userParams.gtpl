<table class="user-params-block">
  <tr>
    <td class="user-params-label">
      <span>Sex:</span>
    </td>
    <td>
      <select class="input sexSelect">
        <% (0..5).each { number -> %> 
          <option value="${number}" title="/images/sex-type-${number}.png"></option>
        <% } %>
      </select>
    </td>
    <td class="user-params-label">
      <span>period of life:</span>
    </td>
    <td>
      <select class="input lifePeriodSelect">
        <% (0..6).each { number -> %> 
          <option value="${number}" title="/images/period-of-life-${number}.png"></option>
        <% } %>
      </select>
    </td>
  </tr>
</table>