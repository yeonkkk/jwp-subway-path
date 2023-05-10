package subway.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import subway.domain.Section;
import subway.domain.Sections;
import subway.domain.Station;

@Repository
public class SectionDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public SectionDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("SECTIONS")
                .usingGeneratedKeyColumns("id");
    }

    public Sections findSectionsByLineId(final long lineId) {

        String sql = "SELECT SEC.id, S1.id, S1.name, S2.id, S2.name, SEC.distance, SEC.next_id"
                + " FROM SECTIONS AS SEC "
                + " JOIN STATION AS S1 ON SEC.up_id = S1.id "
                + " JOIN STATION AS S2 ON SEC.down_id = S2.id "
                + " WHERE SEC.line_id = ?";

        List<Section> sections = jdbcTemplate.query(sql, upStationMapper(), lineId);

        return new Sections(lineId, sections);
    }

    private RowMapper<Section> upStationMapper() {
        return (rs, rowNum) -> new Section(
                rs.getLong(1),
                new Station(rs.getLong(2), rs.getString(3)),
                new Station(rs.getLong(4), rs.getString(5)),
                rs.getInt(6),
                rs.getLong(7));
    }

    public long save(final Section section, final long lineId) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", section.getId())
                .addValue("up_id", section.getUpStationId())
                .addValue("down_id", section.getDownStationId())
                .addValue("distance", section.getDistance())
                .addValue("line_id", lineId)
                .addValue("next_id", section.getNextSectionId());

        return simpleJdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
    }

    public void updateNextSection(long nextId, long sectionId) {
        String sql = "UPDATE SECTIONS SET next_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, nextId, sectionId);
    }

    public void update(final Section section) {
        String sql = "UPDATE SECTIONS SET up_id = ?, down_id = ?, distance = ?, next_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                section.getUpStationId(),
                section.getDownStationId(),
                section.getDistance(),
                section.getNextSectionId(),
                section.getId());
    }
}
